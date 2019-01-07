#!/usr/bin/env python3

import os, sys
import urllib.parse

if len (sys.argv) != 2 :
    print("Usage: python ", sys.argv[0], "<two-column-file. 1. inner-link-count, 2. percent-encoded-title>")
    sys.exit (1)

fileName=sys.argv[1]

with open(fileName) as f:
    for line in f.readlines():
        line = line.strip()
        columns = line.split(' ')
        # print(columns)
        count = columns[0]
        title = columns[1]
        print(urllib.parse.unquote(title), count)
