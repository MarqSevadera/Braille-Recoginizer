package com.example.dannaiso.brailleapplication;

public class CharMatrix {

    private int matrix[][];
    private char value;


    public CharMatrix(int mat[][] ,char value){
        matrix = mat;
        this.value = value;
    }
    public CharMatrix(int mat[][]){
        matrix = mat;
    }

    public char getValue(){
        return value;
    }
    public int[][] getMatrix() {
        return matrix;
    }
}
