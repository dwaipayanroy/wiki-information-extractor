#!/usr/bin/python -w
# Input: <non-en-lang name> <7 column file with 1st and 4th col containing the parallel En and nonEn Wikipedia ids>
# Output: infobox information of the parallel pages in En and non-En languages

import os, sys
import urllib
import urllib.request
import wget

if len (sys.argv) != 3 :
    print("Usage: python ", sys.argv[0], "<non-en-lang> <file-path>")
    sys.exit (1)

nonEnLangName=sys.argv[1]
fname = sys.argv[2]

downloadPath="/home/dwaipayan/wiki/wiki-infobox-download-"+nonEnLangName
os.mkdir(downloadPath)

print(fname)
count=1
with open(fname) as f:
    for line in f.readlines():
        columns = line.split('\t')
        enId = columns[1]
        nonenId = columns[4]
        print()
        url="https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=xmlfm&rvsection=0&pageids="+enId
        print(url)
        wget.download(url, '%s/%d-en.html' % (downloadPath, count))
        #
        url="https://"+nonEnLangName+".wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=xmlfm&rvsection=0&pageids="+nonenId
        print(url)
        wget.download(url, '%s/%d-nonen.html' % (downloadPath, count))
        count += 1
        # exit(0)
