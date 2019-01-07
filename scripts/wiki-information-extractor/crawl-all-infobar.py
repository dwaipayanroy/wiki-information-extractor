#!/usr/bin/python
# Input: <lang-name> <input file with 1st col containing the Wikipedia ids> <Output directory to hold all the infobox information>
# Output: infobox information of the page in the provided language.

import os, sys
import urllib
import urllib.request
import wget

if len (sys.argv) != 4 :
    print("Usage: python ", sys.argv[0], "<lang> <input-file-path> <output-path>")
    sys.exit (1)

lang = sys.argv[1]
fname = sys.argv[2]
downloadPath = sys.argv[3]

# downloadPath="/home/dwaipayan/wiki/wiki-infobox-download-"+nonEnLangName
# os.mkdir(downloadPath)

print(fname)
with open(fname) as f:
    for line in f.readlines():
        docid = line.strip()
        print()
        #
        url="https://"+lang+".wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=xmlfm&rvsection=0&pageids="+docid
        print(url)
        wget.download(url, '%s/%s-infobox.html' % (downloadPath, docid))
        # exit(0)
