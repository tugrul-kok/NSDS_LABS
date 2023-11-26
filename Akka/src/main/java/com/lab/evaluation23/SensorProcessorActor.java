package com.lab.evaluation23;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class SensorProcessorActor extends AbstractActor {

	private double currentAverage;

	private double receivedDataPoints;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
	}

	private void gotData(TemperatureMsg msg) throws Exception {

		//System.out.println("SENSOR PROCESSOR " + self().path().name() + ": Got data from " + msg.getSender());

		if (msg.getTemperature() < 0){
			System.out.println("SENSOR PROCESSOR Temperature is negative");
			throw new Exception("SENSOR PROCESSOR Temperature can not be negative");
		}

		System.out.println("SENSOR PROCESSOR " + self().path().name() + ": Arrived temperature is " + msg.getTemperature());
		currentAverage = (currentAverage* receivedDataPoints + msg.getTemperature()) / (receivedDataPoints + 1);
		receivedDataPoints++;
		System.out.println("SENSOR PROCESSOR " + self().path().name() + ": Current avg is " + currentAverage);
	}

	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}

	public SensorProcessorActor() {
	}
}
