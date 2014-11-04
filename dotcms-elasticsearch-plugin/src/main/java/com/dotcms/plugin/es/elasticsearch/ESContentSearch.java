package com.dotcms.plugin.es.elasticsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dotcms.content.elasticsearch.business.ESContentletAPIImpl;
import com.dotcms.content.elasticsearch.business.IndiciesAPI.IndiciesInfo;
import com.dotcms.content.elasticsearch.util.ESClient;
import com.dotcms.repackage.org.elasticsearch.action.search.SearchPhaseExecutionException;
import com.dotcms.repackage.org.elasticsearch.action.search.SearchRequestBuilder;
import com.dotcms.repackage.org.elasticsearch.action.search.SearchResponse;
import com.dotcms.repackage.org.elasticsearch.client.Client;
import com.dotcms.repackage.org.elasticsearch.index.query.FilterBuilders;
import com.dotcms.repackage.org.elasticsearch.index.query.QueryBuilders;
import com.dotcms.repackage.org.elasticsearch.search.SearchHit;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.Role;
import com.dotmarketing.common.model.ContentletSearch;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;

import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

public class ESContentSearch extends ESContentletAPIImpl {

	enum QueryType {
		search, suggest, moreLike, Facets
	};

	/**
	 * This will only return the list of inodes as hits, and does not load the
	 * contentlets from cache
	 * 
	 * @param esQuery
	 * @param live
	 * @param user
	 * @param respectFrontendRoles
	 * @return
	 * @throws DotSecurityException
	 * @throws DotDataException
	 */
	public SearchResponse esSearchRaw(String esQuery, boolean live, User user, boolean respectFrontendRoles) throws DotSecurityException,
			DotDataException {

		if (!UtilMethods.isSet(esQuery)) {
			throw new DotStateException("ES Query is null");
		}


		String indexToHit;
		IndiciesInfo info;
		try {
			info = APILocator.getIndiciesAPI().loadIndicies();
			if (live)
				indexToHit = info.live;
			else
				indexToHit = info.working;
		} catch (DotDataException ee) {
			Logger.fatal(this, "Can't get indicies information", ee);
			return null;
		}

		List<Role> roles = new ArrayList<Role>();
		if (user == null && !respectFrontendRoles) {
			throw new DotSecurityException("You must specify a user if you are not respecting frontend roles");
		}

		boolean isAdmin = false;
		if (user != null) {
			if (!APILocator.getRoleAPI().doesUserHaveRole(user, APILocator.getRoleAPI().loadCMSAdminRole())) {
				roles = APILocator.getRoleAPI().loadRolesForUser(user.getUserId());
			} else {
				isAdmin = true;
			}
		}

		Client client = new ESClient().getClient();
		SearchRequestBuilder srb = client.prepareSearch(indexToHit);
		QueryType qt = QueryType.search;
		esQuery = esQuery.toLowerCase();
		if (esQuery.contains("\"suggest\"")) {
			qt = QueryType.suggest;
		} else if (esQuery.contains("\"facets\"")) {
			qt = QueryType.Facets;
		}

		if (qt == QueryType.search) {
			srb.addField("inode");
			srb.addField("identifier");
		} else {
			srb.setSize(0);
		}

		srb.setExtraSource(esQuery);

		if (!isAdmin) {
			StringBuffer perms = new StringBuffer();
			addPermissionsToQuery(perms, user, roles, respectFrontendRoles);
			if (perms != null && perms.length() > 0) {
				srb.setFilter(FilterBuilders.queryFilter(QueryBuilders.queryString(perms.toString())).cache(true));
			}
		}

		try {
			SearchResponse resp = null;
			resp = srb.execute().actionGet();

			return resp;
		} catch (SearchPhaseExecutionException e) {
			throw e;
		}

	}

	public ESSearchResults esSearch(String esQuery, boolean live, User user, boolean respectFrontendRoles)
			throws DotSecurityException, DotDataException {

		SearchResponse resp = esSearchRaw(esQuery, live, user, respectFrontendRoles);

		ESSearchResults contents = new ESSearchResults(resp, new ArrayList());

		contents.setQuery(esQuery);


		List<ContentletSearch> list = new ArrayList<ContentletSearch>();
		if (contents.getHits() == null) {
			return contents;
		}

		long start = System.currentTimeMillis();

		for (SearchHit sh : contents.getHits()) {
			try {
				Map<String, Object> hm = new HashMap<String, Object>();
				ContentletSearch conwrapper = new ContentletSearch();

				conwrapper.setInode(sh.field("inode").getValue().toString());

				list.add(conwrapper);
			} catch (Exception e) {
				Logger.error(this, e.getMessage(), e);
			}

		}
		ArrayList<String> inodes = new ArrayList<String>();

		for (ContentletSearch conwrap : list) {
			inodes.add(conwrap.getInode());
		}

		List<Contentlet> contentlets = findContentlets(inodes);
		Map<String, Contentlet> map = new HashMap<String, Contentlet>(contentlets.size());
		for (Contentlet contentlet : contentlets) {
			map.put(contentlet.getInode(), contentlet);
		}
		for (String inode : inodes) {
			if (map.get(inode) != null)
				contents.add(map.get(inode));
		}

		contents.setPopulationTook(System.currentTimeMillis() - start);
		return contents;
	}

}
