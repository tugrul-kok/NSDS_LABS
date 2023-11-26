# Evaluation lab - Akka

## Group number: 48

## Group members

- Nisanur Camuzcu
- Lukas Girschick
- Tugrul Kok

## Description of message flows
When the TemperatureSensorActor receives a GenerateMsg it creates a new TemperatureMsg and calculates a random temperature for this message.
The TemperatureSensorFaultyActor adds a negative temperature to the TemperatureMsg.
The TemperatureMsg is sent as the dispatcher.
If the TemperatureSensorActor or TemperatureSensorFaultyActor receives a ConfigMsg, it adds the reference to the dispatcher.

The dispatcher acts as a Supervisor to the SensorProcessorActors.
If the DispatcherActor is in balanced mode, it takes a TemperatureMsg and balances the forwarding of messages between the attached processors. 
In the round-robin mode the dispatcher iterates through the processors when forwarding the received TemperatureMsg.
The DispatcherActor uses the DispatchLogicMsg to switch between balanced and round-robin strategy for balancing.

The SensorProcessorActor can receive the TemperatureMsg from the dispatcher and throws an Exception if the temperature of the msg is negative. The average is retained because the Dispatcher(Supervisor) has a resume strategy.
