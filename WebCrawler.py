import sys
import re
from bs4 import BeautifulSoup
import urllib.request
import httplib2

value = input("Enter something to print\n\n")
print(value, " here")

def focusedCrawling(seedPage, keyPhrase=None):

    linkColl = {}

    linkColl[1] = "http://en.wikipedia.org/wiki/Hugh_of_Saint-Cher"

    if not keyPhrase:
        linkColl = crawlEachPageWithoutkeyPhrase(1, 1, 1, linkColl)


    print(len(linkColl))
    return linkColl



def crawlEachPageWithoutkeyPhrase(start, end, cnt, linkColl):
    #recursive approach fro crawling pages from a given seed page
    if(len(linkColl) == 1000 or cnt > 5):
        return linkColl
    else:
        endTmp = 0
        print (cnt, "this is count")
        for i in range(start, end+1):
            if len(linkColl) >= 1000:
                break
            else:
                print(i, "this is key")
                print(linkColl.get(i), "this is next to be crawled link")
                response = urllib.request.urlopen(str(linkColl.get(i)))
                html_doc = response.read()
                soup = BeautifulSoup(html_doc, 'html.parser')

                # exclude Main_Page and all other help and index pages follwed by :
                res = re.compile('/wiki/(?!Main_Page)[^:]+',  re.IGNORECASE)

                for link in soup.find_all('a'):
                    if res.fullmatch(str(link.get('href'))):
                        strg = ("http://en.wikipedia.org" + link.get('href'))
                        if len(linkColl) >= 1000:
                            break
                        elif strg in linkColl.values():
                                continue
                        else :
                            linkColl[len(linkColl)+1] = strg
                            endTmp += 1

            print(len(linkColl), "this is current length")
        end += endTmp
        print (end, "this is end")
        cnt += 1
        return crawlEachPageWithoutkeyPhrase(start+1, end, cnt, linkColl)


focusedCrawling("http://en.wikipedia.org/wiki/Hugh_of_Saint-Cher")