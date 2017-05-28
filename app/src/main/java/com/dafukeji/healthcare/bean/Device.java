package com.dafukeji.healthcare.bean;

/**
 * Created by DevCheng on 2017/5/27.
 */

public class Device {

	private String name;
	private String address;
	private int battery;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getBattery() {
		return battery;
	}

	public void setBattery(int battery) {
		this.battery = battery;
	}
}
