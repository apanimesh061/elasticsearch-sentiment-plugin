# elasticsearch-sentiment-plugin

VADER (Valence Aware Dictionary and sEntiment Reasoner) is a lexicon and rule-based sentiment analysis tool that is _specifically attuned to sentiments expressed in social media_. It is fully open-sourced under the [MIT License](http://choosealicense.com/) (we sincerely appreciate all attributions and readily accept most contributions, but please don't hold us liable).

This is a JAVA port of the NLTK VADER sentiment analysis originally written in Python.

 - The [Original](https://github.com/cjhutto/vaderSentiment) python module by the paper's author C.J. Hutto
 - The [NLTK](http://www.nltk.org/_modules/nltk/sentiment/vader.html) source
 
I used the library and created an Ingestion Plugin to create an ingest pipline that takes in a document and for specific field(s) apply Sentiment Analysis on them.

Following is an example of how to use the Plugin:

I used Elasticsearch v5.2.1 with the [VaderSentimentJava v1.0.1](https://github.com/apanimesh061/VaderSentimentJava/releases/tag/v1.0.1) to use the pipeline. Before using the plugin it needs to be installed using the standard way of [installing plugins](https://www.elastic.co/guide/en/elasticsearch/plugins/5.2/installation.html) in Elasticsearch:

For `VaderSentimentJava` do:

    mvn clean
    mvn install:install-file -Dfile=e:\VaderSentimentJava\target\releases\vader-sentiment-analyzer-1.0.jar -DgroupId=com.vader.sentiment -DartifactId=vader-sentiment-analyzer -Dversion=1.0 -Dpackaging=jar

For `elasticsearch-sentiment-plugin` do:

    mvn clean && mvn package
    bin\elasticsearch-plugin install file:///E:/elasticsearch-sentiment-plugin/target/releases/vader-sentiment-ingest-plugin-5.2.1.zip

You can use the [Simulate API](https://www.elastic.co/guide/en/elasticsearch/reference/master/simulate-pipeline-api.html) to simulate the ingestion pipeline that we just installed:

    curl -XPOST http://localhost:9200/_ingest/pipeline/_simulate --header 'content-type: application/json' -d '{
      "pipeline": {
        "description": "Apply VADER sentiment analysis on text.",
        "processors": [
          {
            "vader_analyzer": {
              "input_field": "content",
              "target_field": "polarity"
            }
          }
        ],
        "version": 1
      },
      "docs": [
        {
          "_id": "HGZ1H9j7J3RCzX2CC7XCcg",
          "_parent": "9SPwF-vRgtuHxciFxv5YLA",
          "_source": {
            "review_date": "2014-06-03",
            "review_user_id": "JWtFuKaFXn_l5h-qKZWZuQ",
            "review_stars": 4,
            "content": "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers are extremely inattentive. \nWe always sit in the bar/lounge area, which is beautifully decorated and has quite a fresh, relaxing vibe. \nThe drinks and food are excellent -- I've only had 1 slightly bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. I love it! Go!",
            "review_votes": [
              {
                "count": 0,
                "vote_type": "funny"
              },
              {
                "count": 1,
                "vote_type": "useful"
              },
              {
                "count": 0,
                "vote_type": "cool"
              }
            ]
          }
        }
      ]
    }'

This request will transform the input document by adding `{"polarity":{"negative":0.061,"neutral":0.634,"positive":0.305,"compound":0.9901}}` to the original document:

    {
        "docs": [
            {
                "doc": {
                    "_type": "_type",
                    "_id": "HGZ1H9j7J3RCzX2CC7XCcg",
                    "_parent": "9SPwF-vRgtuHxciFxv5YLA",
                    "_index": "_index",
                    "_source": {
                        "review_user_id": "JWtFuKaFXn_l5h-qKZWZuQ",
                        "review_date": "2014-06-03",
                        "content": "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers are extremely inattentive. \nWe always sit in the bar/lounge area, which is beautifully decorated and has quite a fresh, relaxing vibe. \nThe drinks and food are excellent -- I've only had 1 slightly bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. I love it! Go!",
                        "review_stars": 4,
                        "review_votes": [
                            {
                                "vote_type": "funny",
                                "count": 0
                            },
                            {
                                "vote_type": "useful",
                                "count": 1
                            },
                            {
                                "vote_type": "cool",
                                "count": 0
                            }
                        ],
                        "polarity": {
                            "negative": 0.061,
                            "neutral": 0.634,
                            "positive": 0.305,
                            "compound": 0.9901
                        }
                    },
                    "_ingest": {
                        "timestamp": "2017-10-02T01:35:05.547Z"
                    }
                }
            }
        ]
    }

Now let's see how do we ingest the document without the `Simulate API`:

  First we need to add our pipeline to elastic (here we name it 'sentiment-analyzer'),
  
    curl --request PUT \
      --url 'http://localhost:9200/_ingest/pipeline/sentiment-analyzer \
      --header 'content-type: application/json' \
      --data '{
        "description": "Apply VADER sentiment analysis on text.",
        "processors": [
          {
            "vader_analyzer": {
              "input_field": "content",
              "target_field": "polarity"
            }
          }
        ],
        "version": 1
      }'
      
   which returns:
   
      {"acknowledged":true}
      
      
   Now we can check it
      
      
    curl --request POST \
      --url 'http://localhost:9200/yelp_index/review/HGZ1H9j7J3RCzX2CC7XCcg2?pipeline=sentiment-analyzer' \
      --header 'content-type: application/json' \
      --data '{
      "_parent": "9SPwF-vRgtuHxciFxv5YLA",
      "_id": "HGZ1H9j7J3RCzX2CC7XCcg2",
      "review_date": "2014-06-03",
      "review_user_id": "JWtFuKaFXn_l5h-qKZWZuQ",
      "review_stars": 4,
      "content": "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers are extremely inattentive. \nWe always sit in the bar/lounge area, which is beautifully decorated and has quite a fresh, relaxing vibe. \nThe drinks and food are excellent -- I've only had 1 slightly bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. I love it! Go!",
      "review_votes": [
        {
          "count": 0,
          "vote_type": "funny"
        },
        {
          "count": 1,
          "vote_type": "useful"
        },
        {
          "count": 0,
          "vote_type": "cool"
        }
      ]
    }'
    
 which returns:
 
     {
        "_index": "yelp_index",
        "_type": "review",
        "_id": "HGZ1H9j7J3RCzX2CC7XCcg2",
        "_version": 1,
        "result": "created",
        "_shards": {
            "total": 1,
            "successful": 1,
            "failed": 0
        },
        "created": true
    }

Now let's see how this document has been indexed:

    curl --request POST \
      --url http://localhost:9200/yelp_index/review/_search \
      --header 'content-type: application/json' \
      --data '{
          "query": {
            "terms": {
              "_id": [ "HGZ1H9j7J3RCzX2CC7XCcg2"]
            }
          }
        }'

which returns:

    {
        "took": 6,
        "timed_out": false,
        "_shards": {
            "total": 5,
            "successful": 5,
            "failed": 0
        },
        "hits": {
            "total": 1,
            "max_score": 1,
            "hits": [
                {
                    "_index": "yelp_index",
                    "_type": "review",
                    "_id": "HGZ1H9j7J3RCzX2CC7XCcg2",
                    "_score": 1,
                    "_routing": "9SPwF-vRgtuHxciFxv5YLA",
                    "_parent": "9SPwF-vRgtuHxciFxv5YLA",
                    "_source": {
                        "review_user_id": "JWtFuKaFXn_l5h-qKZWZuQ",
                        "review_date": "2014-06-03",
                        "content": "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers are extremely inattentive. \nWe always sit in the bar/lounge area, which is beautifully decorated and has quite a fresh, relaxing vibe. \nThe drinks and food are excellent -- I've only had 1 slightly bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. I love it! Go!",
                        "review_stars": 4,
                        "review_votes": [
                            {
                                "vote_type": "funny",
                                "count": 0
                            },
                            {
                                "vote_type": "useful",
                                "count": 1
                            },
                            {
                                "vote_type": "cool",
                                "count": 0
                            }
                        ],
                        "polarity": {
                            "negative": 0.061,
                            "neutral": 0.634,
                            "positive": 0.305,
                            "compound": 0.9901
                        }
                    }
                }
            ]
        }
    }
    
So, we see that a new section has been added which is basically application of the VADER on `content` field of the input document and added to the `polarity` section of the result Json.
