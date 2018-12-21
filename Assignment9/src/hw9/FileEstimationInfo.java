/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw9;

/**
 * @author Bo Han
 * This is the helper class for storing each file estimation info
 */
 public class FileEstimationInfo{
    private String fileName;
    private int vowels;
    private int letterNumbers;
    private double estimatorOneDiff;
    private double estimatorTwoDiff;

    public FileEstimationInfo(String fileName) {
         this.fileName = fileName;
         this.vowels = 0;
         this.letterNumbers = 0;
         this.estimatorOneDiff = 0.0;
         this.estimatorTwoDiff = 0.0;
    }

    //Setters
    public void setFileNumbers(int vowels, int letterNumbers){
         this.vowels = vowels;
         this.letterNumbers = letterNumbers;
    }

    public void setEstimatorOneDiff(double estimatorOneDiff){
        this.estimatorOneDiff = estimatorOneDiff;
    }
    public void setEstimatorTwoDiff(double estimatorTwoDiff){
        this.estimatorTwoDiff = estimatorTwoDiff;
    }

    //Getters
    public String getFileName(){
        return fileName;
    }

    public int getVowelsInFile(){
        return vowels;
    }

    public int getLetterNumbersInFile(){
        return letterNumbers;
    }

    public double getPercent(){
        return (double)vowels/(double)letterNumbers;
    }

    public double getEstimatorOneDiff(){
                return estimatorOneDiff;
            }
    public double getEstimatorTwoDiff(){
                return estimatorTwoDiff;
            }

    }
