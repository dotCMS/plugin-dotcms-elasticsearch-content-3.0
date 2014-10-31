This plugin for dotCMS 3.0 exposes native elasticsearch content queries via a viewtool, a java api, a portlet and as a restful service.  It allows you access to geolocation queries, facets, suggestions ("Did you mean?" and other OTB functionality.

In order to use geolocation, you will need to add a field to the content you want to query with the velocity var name "latlon".

Once this plugin is installed, you can add the portlet to a layout and try elasticsearch queries against the dotCMS content store.  

## Viewtool
```
#set($results = $estool.search('{
    "query": {
        "bool": {
            "must": {
                "term": {
                    "title": "gas"
                }
            },
            "must_not": {
                "range": {
                    "languageid": {
                        "from": 2,
                        "to": 20
                    }
                }
            }
        }
    }
}'
))

#foreach($con in $results)
  $con.title<br>
#end
<hr>
$results.response<br>

```

## RESTful

Curl for  results
```
curl -XGET http://localhost:8080/api/es/search -d '{
    "query": {
        "bool": {
            "must": {
                "term": {
                    "_all": "gas"
                }
            }
        }
    }
}'
```
curl for facets (raw endpoint gives you the the raw SearchResponse from ElasticSearch
```
curl -XGET http://localhost:8080/api/es/raw -d '
	{
	    "query" : { "query_string" : {"query" : "gas*"} },
	    "facets" : {
	        "tags" : { "terms" : {"field" : "news.tags"} }
	    }
	}
'
```

curl for suggestions (Did you mean?)
```
curl -XGET http://localhost:8080/api/es/raw -d '
	{
	  "suggest" : {
	    "title-suggestions" : {
	      "text" : "gs pric rollrcoater",
	      "term" : {
	        "size" : 3,
	        "field" : "title"
	      }
	    }
	  }
	}
'
```

## Queries (use the portlet to see results)
Match All
```
{
    "query" : {
        "match_all" : {}
    }
}
```

Match "gas"
```
{
    "query": {
        "bool": {
            "must": {
                "term": {
                    "_all": "gas"
                }
            }
        }
    }
}
```

Facet on the news.tags field
```
{
    "query" : { "query_string" : {"query" : "gas*"} },
    "facets" : {
        "tags" : { "terms" : {"field" : "news.tags"} }
    }
}
```

Suggest based on title
```
{
  "suggest" : {
    "title-suggestions" : {
      "text" : "gs pric rollrcoater",
      "term" : {
        "size" : 3,
        "field" : "title"
      }
    }
  }
}
```

Query using a range
```
{
    "query": {
        "bool": {
            "must": {
                "term": {
                    "title": "gas"
                }
            },
            "must_not": {
                "range": {
                    "languageid": {
                        "from": 2,
                        "to": 20
                    }
                }
            }
        }
    }
}
```


## Geolocation
filter news by distance away

(For this example to work you need to add a field to the news structure 
that uses latlon as its velocity variable name.
it can be a text field with a value of ""42.648899,-71.165497)
```
{
    "query": {
        "filtered": {
            "query": {
                "match_all": {}
            },
            "filter": {
                "geo_distance": {
                    "distance": "20km",
                    "news.latlon": {
                        "lat": 37.776,
                        "lon": -122.41
                    }
                }
            }
        }
    }
}
```

// filter news by distance away part 2
// (For this example to work you need to add a field to the news structure 
// that uses latlon as its velocity variable name.
// it can be a text field with a value of ""42.648899,-71.165497)
```
{
    "query": {
        "filtered": {
            "query": {
                "match_all": {}
            },
            "filter": {
                "geo_distance": {
                    "distance": "20km",
                    "news.latlon": {
                        "lat": 42.648899,
                        "lon": -71.165497
                    }
                }
            }
        }
    }
}
```

// sort news by distance away
// (For this example to work you need to add a field to the news structure 
// that uses latlon as its velocity variable name.
// it can be a text field with a value of ""42.648899,-71.165497)
```
{
    "sort" : [
        {
            "_geo_distance" : {
                "news.latlon" : {
                    "lat" : 42,
                    "lon" : -71
                },
                "order" : "asc",
                "unit" : "km"
            }
        }
    ],
    "query" : {
        "term" : { "title" : "gas" }
    }
}
```

