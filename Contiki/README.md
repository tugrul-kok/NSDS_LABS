# Evaluation lab - Contiki-NG

## Solution description

The function "is_empty()" in udp-client, checks if the array is empty by iterating through its elements and returning 0 if a non-zero element is found, and 1 otherwise. The second function, "get_average()", calculates the average of non-zero elements by iterating through the array, summing values and counting non-zero occurrences, and then returning the average. The third function, "clear_readings()", sets all elements in the array to zero, ensuring that subsequent calls to "is_empty()" will return 1, indicating an empty array. Udp-client waits for the timer, when the timer expires, the client checks network connectivity and processes temperature readings accordingly. If connected, it sends either individual readings or the average of saved readings to the root node. If disconnected, it stores the current reading in an array, managing the index to prevent array overflow.


Udp-server, when gets a reading, it checks if the client was known before. If it is a new client, the IP address of the client is added to known addresses. When udp-server recieves a new reading, it calculates the averages of the last MAX_READINGS temperature values.