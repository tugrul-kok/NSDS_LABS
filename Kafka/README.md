# Evaluation lab - Apache Kafka

## Group number: 48

## Group members

- Nisanur Camuzcu
- Tugrul Kok
- Lukas Girschick

## Exercise 1

- Number of partitions allowed for inputTopic (1, N)
- Number of consumers allowed (1, N)
    - Consumer 1: groupA
    - Consumer 2: groupA
    - ...
    - Consumer n: groupA

The number of partitions have to be greater or equal than the number of consumers if they all have the same group name. The group of the Consumers can vary, but this would defeat the principle of dividing work across consumers. 

## Exercise 2

- Number of partitions allowed for inputTopic (1, N)
- Number of consumers allowed (1, 1)
    - Consumer 1: groupB

Multiple consumers are only allowed if they are in different groups. These Consumers would calculate the same Result as they process the same messages.
