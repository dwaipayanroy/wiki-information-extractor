#!/usr/bin/python -w
## Crawls the Wikipedia titles of the pages, correspnding to the given language and wikipedia-curid in the language
# Input:  <lang-name> <tsv-file-path-containing-wikipedia-curid-in-1st-col>
# Output: Prints <title>...</title> on the screen of the Wikipedia pages provided in the file.

import os, sys
import urllib
import urllib.request
import wget
import requests
import re

if len (sys.argv) != 3 :
    print("Usage: python ", sys.argv[0], "<lang-name> <tsv-file-path-containing-curid-in-1st-col>")
    sys.exit (1)

langName=sys.argv[1]
fname = sys.argv[2]

# downloadPath="/home/dwaipayan/wiki/wiki-infobox-download-"+nonEnLangName
# os.mkdir(downloadPath)

print(fname)
count=1
with open(fname) as f:
    for line in f.readlines():
        columns      = line.split('\t')
        curId=columns[0]
        url="https://"+langName+".wikipedia.org/?curid="+curId
        print(url)
        output = requests.get(url).text
        # output = requests.get("https://hi.wikipedia.org/?curid=62379").text
        match=re.search(r'<title>.*</title>', output)
        print(match.group(0))
