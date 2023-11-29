# Evaluation lab - Node-RED

## Group number: 48

## Group members

 - Nisanur Camuzcu
 - Lukas Girschick
 - Tugrul Kok

## Description of message flows
The flow starts with setting the Telegram Receiver, with the given API. The “Compute Answer” function node with two outputs sets the types for the answer by checking the location, type, and time. According to the these parameters, the related values are obtained from open weather node. Then the next function arranges the structure of the answer as given in the assignment by obtaining requested values with respect to those parameters, from open weather. The full answer is sent to the Text node, and then to the Telegram Sender. In order to store the query, the parameters are defined separately and combined to be held in the log file.

## Extensions 
There are no extra extensions other than used in courses (node-red-contrib-chatbot, node-red-node-openweathermap)
## Bot URL 
Telegram bot API: 6756170840:AAFqs6ipvHmF2XFsZsZLCK1UxykM3RXTe50
