package com.MWBFServer.Stats;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.MWBFServer.Users.User;

@Entity
@Table (name="USER_STATS")
public class PersonalStats implements Serializable
{
	private static final long serialVersionUID = -1675541751894878366L;
	
	private double weight;
	private double bodyFatPct;
	private User user;
	
	@Column (name="WEIGHT")
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Column (name="BODY_FAT_PCT")
	public double getBodyFatPct() {
		return bodyFatPct;
	}
	public void setBodyFatPct(double bodyFatPct) {
		this.bodyFatPct = bodyFatPct;
	}
	
	@Id @ManyToOne
    @JoinColumn(name = "user_id")
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public String toString()
	{
		return "Weight["+weight+"], BodyFatPct[" + bodyFatPct + "]";
	}

}
