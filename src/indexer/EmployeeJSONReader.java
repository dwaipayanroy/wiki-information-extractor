/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.util.Arrays;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;



class Address {
	
	private String street;
	private String city;
	private int zipcode;
	
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getZipcode() {
		return zipcode;
	}
	public void setZipcode(int zipcode) {
		this.zipcode = zipcode;
	}
	
	@Override
	public String toString(){
		return getStreet() + ", "+getCity()+", "+getZipcode();
	}
}


class Employee {

	private int id;
	private String name;
	private boolean permanent;
	private Address address;
	private long[] phoneNumbers;
	private String role;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isPermanent() {
		return permanent;
	}
	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public long[] getPhoneNumbers() {
		return phoneNumbers;
	}
	public void setPhoneNumbers(long[] phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("***** Employee Details *****\n");
		sb.append("ID="+getId()+"\n");
		sb.append("Name="+getName()+"\n");
		sb.append("Permanent="+isPermanent()+"\n");
		sb.append("Role="+getRole()+"\n");
		sb.append("Phone Numbers="+Arrays.toString(getPhoneNumbers())+"\n");
		sb.append("Address="+getAddress());
		sb.append("\n*****************************");
		
		return sb.toString();
	}
}


public class EmployeeJSONReader {

	public static final String JSON_FILE="/home/dwaipayan/Dropbox/programs/Wikipedia/src/indexer/json-input.txt";
	
	public static void main(String[] args) throws IOException {
		InputStream fis = new FileInputStream(JSON_FILE);
		
		//create JsonReader object
		JsonReader jsonReader = Json.createReader(fis);
		
		/**
		 * We can create JsonReader from Factory also
		JsonReaderFactory factory = Json.createReaderFactory(null);
		jsonReader = factory.createReader(fis);
		*/
		
		//get JsonObject from JsonReader
		JsonObject jsonObject = jsonReader.readObject();
		
		//we can close IO resource and JsonReader now
		jsonReader.close();
		fis.close();
		
		//Retrieve data from JsonObject and create Employee bean
		Employee emp = new Employee();
		
		emp.setId(jsonObject.getInt("id"));
		emp.setName(jsonObject.getString("name"));
		emp.setPermanent(jsonObject.getBoolean("permanent"));
		emp.setRole(jsonObject.getString("role"));
		
		//reading arrays from json
		JsonArray jsonArray = jsonObject.getJsonArray("phoneNumbers");
		long[] numbers = new long[jsonArray.size()];
		int index = 0;
		for(JsonValue value : jsonArray){
			numbers[index++] = Long.parseLong(value.toString());
		}
		emp.setPhoneNumbers(numbers);
		
		//reading inner object from json object
		JsonObject innerJsonObject = jsonObject.getJsonObject("address");
		Address address = new Address();
		address.setStreet(innerJsonObject.getString("street"));
		address.setCity(innerJsonObject.getString("city"));
		address.setZipcode(innerJsonObject.getInt("zipcode"));
		emp.setAddress(address);
		
		//print employee bean information
		System.out.println(emp);
                System.out.println(jsonObject.getString("translation"));
		
	}

}