package com.lab.evaluation23;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DispatcherActor extends AbstractActorWithStash {

	private final static int NO_PROCESSORS = 2;

	private HashMap<ActorRef, ActorRef> balancedMapping; //<TemperatureSensorActor, Processor>

	private HashMap<ActorRef, Integer> numberOfAssignedSensors;	//<Processor, number of TemperatureSensorActors>

	private LinkedList<ActorRef> processors;

	private int counter;

	private static SupervisorStrategy strategy =
			new OneForOneStrategy(
					1,
					Duration.ofMinutes(1),
					DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume()).build()
			);

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	public DispatcherActor() {
		processors = new LinkedList<>();
		balancedMapping = new HashMap<>();
		numberOfAssignedSensors = new HashMap<>();

		for(int i = 0; i< NO_PROCESSORS; i++)
		{
			ActorRef processor = getContext().actorOf(SensorProcessorActor.props() , "processor"+ i);
			processors.add(processor);
			numberOfAssignedSensors.put(processor, 0);
		}


	}


	@Override
	public Receive createReceive() {
		return createReceiveOnLoadBalance();
	}

	private Receive createReceiveOnLoadBalance()
	{
		return receiveBuilder()
				.match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
				.match(DispatchLogicMsg.class, this::onDispatchLogic)
				.build();
	}

	private Receive createReceiveOnRoundRobin()
	{
		return receiveBuilder()
				.match(TemperatureMsg.class, this::dispatchDataRoundRobin)
				.match(DispatchLogicMsg.class, this::onDispatchLogic)
				.build();
	}

	void onDispatchLogic(DispatchLogicMsg msg)
	{
		if (msg.getLogic() == DispatchLogicMsg.LOAD_BALANCER)
		{
			getContext().become(createReceiveOnLoadBalance());
			System.out.println("DISPATCHER Switch to Load Balance");
		}
		else if(msg.getLogic() == DispatchLogicMsg.ROUND_ROBIN)
		{
			getContext().become(createReceiveOnRoundRobin());
			System.out.println("DISPATCHER Switch to Round Robin");
		}
	}

	private void dispatchDataLoadBalancer(TemperatureMsg msg)
	{
		if (!balancedMapping.containsKey(msg.getSender()))
		{
			ActorRef processor = null;
			int min = Integer.MAX_VALUE;
			for (Map.Entry<ActorRef, Integer> entry : numberOfAssignedSensors.entrySet())
			{
				if(entry.getValue() < min)
				{
					min = entry.getValue();
					processor = entry.getKey();
				}
			}

			numberOfAssignedSensors.put(processor, numberOfAssignedSensors.get(processor) +1);

			if(processor != null) balancedMapping.put(msg.getSender(), processor);
		}

		balancedMapping.get(msg.getSender()).tell(msg, self());

		//printMaps();
	}

	private void dispatchDataRoundRobin(TemperatureMsg msg)
	{
		processors.get(counter % processors.size()).tell(msg, self());
		counter++;
	}

	static Props props() {
		return Props.create(DispatcherActor.class);
	}

	private void printMaps()
	{
		//System.out.println("State numberMap: Processor0 " + numberOfAssignedSensors.get(processors.get(0)).doubleValue());
		//System.out.println("State numberMap: Processor1 " + numberOfAssignedSensors.get(processors.get(1)).doubleValue());

		for (Map.Entry<ActorRef, ActorRef> entry : balancedMapping.entrySet())
		{
			System.out.println(entry.getKey().path().name() + " sends to " + entry.getValue().path().name());
		}
	}
}
