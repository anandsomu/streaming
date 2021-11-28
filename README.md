# streaming

This project propagates changes from one DB(source) to other DB (target) in continous way.

## Event Listener
Service which listens to changes in source DB and pushes the changes to Queue (RabbitMQ).

## Event Subscriber
Service which tracks the DB changes pushed to the Queue parses it and propogates the chnages to the Target DB.
