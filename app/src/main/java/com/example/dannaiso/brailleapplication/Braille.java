package com.example.dannaiso.brailleapplication;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Braille {

    private static ArrayList<CharMatrix> brailleDB = new ArrayList<>();
    private static int a[][] = {{1,0}, {0,0} , {0,0}};
    private static int b[][] = {{1,0}, {1,0} , {0,0}};
    private static int c[][] = {{1,1}, {0,0} , {0,0}};
    private static int d[][] = {{1,1}, {0,1} , {0,0}};
    private static int e[][] = {{1,0}, {0,1} , {0,0}};
    private static int f[][] = {{1,1}, {1,0} , {0,0}};
    private static int g[][] = {{1,1}, {1,1} , {0,0}};
    private static int h[][] = {{1,0}, {1,1} , {0,0}};
    private static int i[][] = {{0,1}, {1,0} , {0,0}};
    private static int j[][] = {{0,1}, {1,1} , {0,0}};
    private static int k[][] = {{1,0}, {0,0} , {1,0}};
    private static int l[][] = {{1,0}, {1,0} , {1,0}};
    private static int m[][] = {{1,1}, {0,0} , {1,0}};
    private static int n[][] = {{1,1}, {0,1} , {1,0}};
    private static int o[][] = {{1,0}, {0,1} , {1,0}};
    private static int p[][] = {{1,1}, {1,0} , {1,0}};
    private static int q[][] = {{1,1}, {1,1} , {1,0}};
    private static int r[][] = {{1,0}, {1,1} , {1,0}};
    private static int s[][] = {{0,1}, {1,0} , {1,0}};
    private static int t[][] = {{0,1}, {1,1} , {1,0}};
    private static int u[][] = {{1,0}, {0,0} , {1,1}};
    private static int v[][] = {{1,0}, {1,0} , {1,1}};
    private static int w[][] = {{0,1}, {1,1} , {0,1}};
    private static int x[][] = {{1,1}, {0,0} , {1,1}};
    private static int y[][] = {{1,1}, {0,1} , {1,1}};
    private static int z[][] = {{1,0}, {0,1} , {1,1}};
    private static int comma[][] = {{0,0}, {1,0} , {0,0}};
    private static int semi[][] =  {{0,0}, {1,0} , {1,0}};
    private static int colon[][] = {{0,0}, {1,1} , {0,0}};
    private static int dot[][] =   {{0,0}, {1,1} , {0,1}};
    private static int quest[][] = {{0,0}, {1,0} , {1,1}};
    private static int ex[][] =    {{0,0}, {1,1} , {1,0}};
    private static int openQuote[][] = {{0,0}, {1,0} , {1,1}};
    private static int closeQuote[][] = {{0,0}, {0,1} , {1,1}};
    private static int singleQuote[][] = {{0,0}, {0,0} , {1,0}};
    private static int ask[][] = {{0,0}, {0,1} , {1,0}};
    private static int dash[][] = {{0,0}, {0,0} , {1,1}};
    private static int slash[][] = {{0,1}, {0,0} , {1,0}};

    public static void init(){
        brailleDB.add(new CharMatrix(a,'a'));
        brailleDB.add(new CharMatrix(b,'b'));
        brailleDB.add(new CharMatrix(c,'c'));
        brailleDB.add(new CharMatrix(d,'d'));
        brailleDB.add(new CharMatrix(e,'e'));
        brailleDB.add(new CharMatrix(f,'f'));
        brailleDB.add(new CharMatrix(g,'g'));
        brailleDB.add(new CharMatrix(h,'h'));
        brailleDB.add(new CharMatrix(i,'i'));
        brailleDB.add(new CharMatrix(j,'j'));
        brailleDB.add(new CharMatrix(k,'k'));
        brailleDB.add(new CharMatrix(l,'l'));
        brailleDB.add(new CharMatrix(m,'m'));
        brailleDB.add(new CharMatrix(n,'n'));
        brailleDB.add(new CharMatrix(o,'o'));
        brailleDB.add(new CharMatrix(p,'p'));
        brailleDB.add(new CharMatrix(q,'q'));
        brailleDB.add(new CharMatrix(r,'r'));
        brailleDB.add(new CharMatrix(s,'s'));
        brailleDB.add(new CharMatrix(t,'t'));
        brailleDB.add(new CharMatrix(u,'u'));
        brailleDB.add(new CharMatrix(v,'v'));
        brailleDB.add(new CharMatrix(w,'w'));
        brailleDB.add(new CharMatrix(x,'x'));
        brailleDB.add(new CharMatrix(y,'y'));
        brailleDB.add(new CharMatrix(z,'z'));
        brailleDB.add(new CharMatrix(comma,','));
        brailleDB.add(new CharMatrix(semi,';'));
        brailleDB.add(new CharMatrix(colon,':'));
        brailleDB.add(new CharMatrix(dot,'.'));
        brailleDB.add(new CharMatrix(quest,'?'));
        brailleDB.add(new CharMatrix(ex,'!'));
        brailleDB.add(new CharMatrix(openQuote,'"'));
        brailleDB.add(new CharMatrix(closeQuote,'"'));
        brailleDB.add(new CharMatrix(singleQuote,'\''));
        brailleDB.add(new CharMatrix(ask,'*'));
        brailleDB.add(new CharMatrix(dash,'-'));
        brailleDB.add(new CharMatrix(slash,'/'));
    }

    public static String parseMatrix(ArrayList<CharMatrix> matrices){
        StringBuilder sb = new StringBuilder();

        for(int _i = 0; _i < matrices.size(); _i++ ){
            CharMatrix matrix = matrices.get(_i);
            for(int _j = 0; _j < brailleDB.size(); _j++){
                CharMatrix chr = brailleDB.get(_j);
                if(Arrays.deepEquals(matrix.getMatrix(), chr.getMatrix())){
                    sb.append(chr.getValue());
                    break;
                }
            }
        }

        String res = sb.toString();
        Log.i("RES" , res);
        return res;
    }
}
