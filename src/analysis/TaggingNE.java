/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 *
 * @author Dwaipayan <dwaipayan.roy@gmail.com>
 */
public class TaggingNE {
    public static void main(String[] args) {
        String a = "I like watching movies";
        MaxentTagger tagger =  new MaxentTagger("taggers/english-left3words-distsim.tagger");
        String tagged = tagger.tagString(a);
        System.out.println(tagged);    
    }
}
