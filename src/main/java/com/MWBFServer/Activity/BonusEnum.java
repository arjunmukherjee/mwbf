package com.MWBFServer.Activity;

public enum BonusEnum 
{
	CrossTrainingBonus(15);

	private final double m_number;
	
	private BonusEnum(double _value) 
    { 
    	this.m_number = _value; 
    }
    public double getValue() 
    { 
    	return m_number; 
    }
}
