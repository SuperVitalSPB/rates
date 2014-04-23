package ru.supervital.rates;

import java.util.ArrayList;

public class Rate {
//    public ImageView imageView;
	public String Code;
    public double Rate;
    public double RatePrev;
    public String Nominal;
    public String Name;
    public ArrayList<CurrDynam> Dynam;
    public String ID;
    public String NumCode;
    public double maxRate;
    public double minRate;
	public ArrayList<Number> Dates = new ArrayList<Number>(); 
	public ArrayList<Number> Rates = new ArrayList<Number>();
	public boolean isPrevLoaded = false;
    
    public Rate(String Code, String Nominal, String Name){
    	super();
    	this.Code = Code;
    	this.Nominal = Nominal;
    	this.Name = Name;    	
    }
    
}


