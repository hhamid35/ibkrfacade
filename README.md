# IBKR gateway, an Interactive Brokers API wrapper
This project is built around the [Interactive Broker's TWS library](https://interactivebrokers.github.io/tws-api/) for Java. If you have a TWS or IB Gateway running, you can use this as a market data source for other projects, or even as a market gateway for trading bots.

**In this project you can find:**

- The basic functions of the TWS API exposed to a Rest API
	- Searching in Interactive Brokers' instrument master data
	- Subscribe to market data for certain instruments
	- Create / manager orders
- Market data streamed into a Redis server from where you can do it whatever you want (eg.: using as a data source by creating a pub-sub message queue; or stream it to a time series database for further analitycs, build historical datasets, etc.)

## Prerequisites

### TWS or IB gateway
For having acces to market data you will need an account at Interactive Brokers with subscription to those markets and instruments you need.

Furthermore you have to install a [Trading Workstation](https://www.interactivebrokers.com/en/index.php?f=14099#tws-software) (TWS) or an [IB Gateway](https://www.interactivebrokers.com/en/?f=/en/trading/ibgateway-stable.php) which provide access to Interactive Brokers' infrastructure. If you are using TWS, make sure that the API connection is enabled, and if you want to placing orders through the library, the connection is not restricted to "read-only".

**See more:** https://interactivebrokers.github.io/tws-api/initial_setup.html

### Redis server
Redis is a powerful tool for further data distribution or processing. If you have a Redis server up and running, you can configure its access through the application.properties file, then the library will send the market data to the Redis for the subscribed instruments.

## How to use
