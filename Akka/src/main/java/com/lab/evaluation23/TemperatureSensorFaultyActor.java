package com.lab.evaluation23;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class TemperatureSensorFaultyActor extends TemperatureSensorActor {

	private ActorRef dispatcher;
	private final static int FAULT_TEMP = -50;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(GenerateMsg.class, this::onGenerate)
				.match(ConfigMsg.class, this::onConfig)
				.build();
	}

	void onConfig(ConfigMsg msg)
	{
		dispatcher = msg.getActorRef();
	}

	private void onGenerate(GenerateMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Sensing temperature!");
		dispatcher.tell(new TemperatureMsg(FAULT_TEMP,self()), self());
	}

	static Props props() {
		return Props.create(TemperatureSensorFaultyActor.class);
	}

}
