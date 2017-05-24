package com.example;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class DaoGenerator {

	public static void main(String[] args){
		Schema schema=new Schema(1,"com.dafukeji.daogenerator");
		Entity point=schema.addEntity("Point");
		point.addLongProperty("currentTime");
		point.addIdProperty().primaryKey().autoincrement();
		point.addFloatProperty("temperature");
		Property cureId=point.addLongProperty("cureId").getProperty();

		Entity cure=schema.addEntity("Cure");
		cure.addLongProperty("startTime");
		cure.addLongProperty("stopTime");
		cure.addIntProperty("cureType");
		cure.addIdProperty().primaryKey().autoincrement();
//		Property pointId=cure.addLongProperty("pointId").getProperty();

		point.addToOne(cure,cureId);
		cure.addToMany(point,cureId).setName("points");

		try {
			new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema,"app/src/main/java");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
