import os, sys
import glob   

if len (sys.argv) != 2 :
    print(len(sys.argv))
    print("Usage: python", sys.argv[0], "<dir. path containing all ROUGE score>")
    sys.exit(1)

dirPath=sys.argv[1]+"*"
files=glob.glob(dirPath)

rl_avg_recall = 0
rl_avg_precision = 0
rl_avg_fscore = 0

r1_avg_recall = 0
r1_avg_precision = 0
r1_avg_fscore = 0

r2_avg_recall = 0
r2_avg_precision = 0
r2_avg_fscore = 0

rs_avg_recall = 0
rs_avg_precision = 0
rs_avg_fscore = 0

fileCount = 0
for file in files:
    fileCount += 1
    # print("File no: %s" % fileCount)
    f=open(file, 'r')
    lines = f.readlines()
    lineCount=1
    for line in lines:
        values = line.split(",")

        if (lineCount == 2):
            rl_avg_recall += float(values[3])
            rl_avg_precision += float(values[4])
            rl_avg_fscore += float(values[5])
        if (lineCount == 4):
            r1_avg_recall += float(values[3])
            r1_avg_precision += float(values[4])
            r1_avg_fscore += float(values[5])
        if (lineCount == 6):
            r2_avg_recall += float(values[3])
            r2_avg_precision += float(values[4])
            r2_avg_fscore += float(values[5])
        if (lineCount == 8):
            rs_avg_recall += float(values[3])
            rs_avg_precision += float(values[4])
            rs_avg_fscore += float(values[5])
        lineCount += 1
    sys.stdout.write(f.read())
    f.close()

print("Metric Avg_Recall Avg_Precision Avg_F-Score")
print("ROUGE-L %.4f %.4f %.4f" % (rl_avg_recall/fileCount, rl_avg_precision/fileCount, rl_avg_fscore/fileCount))
print("ROUGE-1 %.4f %.4f %.4f" % (r1_avg_recall/fileCount, r1_avg_precision/fileCount, r1_avg_fscore/fileCount))
print("ROUGE-2 %.4f %.4f %.4f" % (r2_avg_recall/fileCount, r2_avg_precision/fileCount, r2_avg_fscore/fileCount))
print("ROUGE-SU4 %.4f %.4f %.4f" % (rs_avg_recall/fileCount, rs_avg_precision/fileCount, rs_avg_fscore/fileCount))

