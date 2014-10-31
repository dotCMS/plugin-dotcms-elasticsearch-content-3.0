package com.dotcms.plugin.es.elasticsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.dotcms.repackage.com.google.gson.Gson;
import com.dotcms.repackage.com.google.gson.GsonBuilder;
import com.dotcms.repackage.com.google.gson.JsonElement;
import com.dotcms.repackage.com.google.gson.JsonParser;
import com.dotcms.repackage.org.elasticsearch.action.search.SearchResponse;
import com.dotcms.repackage.org.elasticsearch.search.SearchHits;
import com.dotcms.repackage.org.elasticsearch.search.facet.Facets;
import com.dotcms.repackage.org.elasticsearch.search.suggest.Suggest;

public class ESSearchResults<Contentlet> implements List<Contentlet> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String scrollId;

	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

	long count;
	String query;
	long queryTook = 0;
	long populationTook = 0;
	Suggest suggestions;
	Facets facets;
	SearchResponse response;

	public SearchResponse getResponse() {
		return response;
	}

	public void setResponse(SearchResponse response) {
		this.response = response;
	}

	SearchHits hits;
	long totalResults;

	public long getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(long totalResults) {
		this.totalResults = totalResults;
	}

	public SearchHits getHits() {
		return hits;
	}

	public void setHits(SearchHits hits) {
		this.hits = hits;
	}

	public Suggest getSuggestions() {
		return suggestions;
	}

	public Facets getFacets() {
		return facets;
	}

	public void setFacets(Facets facets) {
		this.facets = facets;
	}

	public void setSuggestions(Suggest suggestions) {
		this.suggestions = suggestions;
	}

	List<Contentlet> contentlets = new ArrayList<Contentlet>();

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public long getQueryTook() {
		return queryTook;
	}

	public void setQueryTook(long queryTook) {
		this.queryTook = queryTook;
	}

	public long getPopulationTook() {
		return populationTook;
	}

	public void setPopulationTook(long populationTook) {
		this.populationTook = populationTook;
	}

	public List<Contentlet> getContentlets() {
		return contentlets;
	}

	public void setContentlets(List<Contentlet> contentlets) {
		this.contentlets = contentlets;
	}

	@Override
	public int size() {

		return contentlets.size();
	}

	@Override
	public boolean isEmpty() {
		return contentlets.isEmpty();
	}

	@Override
	public boolean contains(Object o) {

		return contentlets.contains(o);
	}

	@Override
	public Iterator<Contentlet> iterator() {

		return contentlets.iterator();
	}

	@Override
	public Object[] toArray() {

		return contentlets.toArray();
	}

	@Override
	public <Contentlet> Contentlet[] toArray(Contentlet[] a) {

		return contentlets.toArray(a);
	}

	@Override
	public boolean add(Contentlet e) {

		return contentlets.add(e);
	}

	@Override
	public boolean remove(Object o) {

		return contentlets.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {

		return contentlets.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Contentlet> c) {
		return contentlets.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Contentlet> c) {
		return contentlets.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return contentlets.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return contentlets.retainAll(c);
	}

	@Override
	public void clear() {
		contentlets.clear();

	}

	@Override
	public Contentlet get(int index) {
		return contentlets.get(index);
	}

	@Override
	public Contentlet set(int index, Contentlet element) {
		return contentlets.set(index, element);
	}

	@Override
	public void add(int index, Contentlet element) {
		contentlets.add(index, element);

	}

	@Override
	public Contentlet remove(int index) {

		return contentlets.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return contentlets.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return contentlets.lastIndexOf(o);
	}

	@Override
	public ListIterator<Contentlet> listIterator() {
		return contentlets.listIterator();
	}

	@Override
	public ListIterator<Contentlet> listIterator(int index) {
		return contentlets.listIterator(index);
	}

	@Override
	public List<Contentlet> subList(int fromIndex, int toIndex) {
		return contentlets.subList(fromIndex, toIndex);
	}

	@Override
	public String toString() {
		return "ESSearchResults [response=" + response + "]";
	}
	
	
	
	

}
