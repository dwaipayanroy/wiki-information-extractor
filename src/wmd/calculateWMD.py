import argparse
import spacy
import wmd

# def argparser():
parser = argparse.ArgumentParser(description="Compute Word Mover\'s Distance")
parser.add_argument('-reference', type=str, help='Reference File')
parser.add_argument('-candidate', type=str, help='Candidate file')

args = parser.parse_args()
# print(args)

print("Loading models...")
nlp = spacy.load('en_core_web_md')
nlp.add_pipe(wmd.WMD.SpacySimilarityHook(nlp), last=True)
print("Models loaded.")

# exit
articleCount = 0
sumWMD = 0
with open(args.reference, 'r') as reference, open(args.candidate, 'r') as candidate:
    for x, y in zip(reference, candidate):
        articleCount += 1
        print("Article pair: %s - " % articleCount, end="")
        x = x.strip()
        y = y.strip()
        # print("{0}\t{1}".format(x, y))
        doc1 = nlp(x)
        doc2 = nlp(y)
        sumWMD += doc1.similarity(doc2)
        print(doc1.similarity(doc2))

print("Average WMD: %.4f" % (sumWMD/articleCount))
