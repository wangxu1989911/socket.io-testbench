"use strict";
const config = require ('./config.json');
const winston = require('winston');
const http = require('http');
const app = require('express')();
const SocketServer = require('socket.io');
const RedisAdapter = require('./socket.io-ioredis');

const logger = new winston.Logger({
  transports: [
    new (winston.transports.Console)({
      colorize: true,
      timestamp: true,
      prettyPrint: true,
    })
  ]
});

const httpServer = http.createServer(app);
httpServer.listen(config.port, () => {
  logger.info('socket.io http server listening on: %s', config.port);
});

const io = new SocketServer(httpServer);
io.adapter(new RedisAdapter());

app.get('/', function (req, res) {
  res.sendfile(__dirname + '/index.html');
});

io.on('connection', (socket) => {
  const {userId} = socket.handshake.query;

  let messageReceived=0;

  logger.info(`Client ${userId} has connected to server (${socket.id}).`);

  socket.on('joinRoom', (message, callback) => {
    try{
      socket.join(message.roomId);
      logger.info(`Client ${userId} has joined room (${message.roomId}).`);
    } catch(err){
      typeof callback === 'function' && callback(err);
    }

    typeof callback === 'function' && callback(null, messageReceived);
  });

  socket.on('message', (message, callback) => {
    try{
      messageReceived = messageReceived + 1;
      socket.to(message.roomId).emit("message", message.content);
    }catch(err){
      typeof callback === 'function' && callback(err);
    }

    typeof callback === 'function' && callback(null, messageReceived);
  });

  socket.on('disconnect', () => {
    logger.info(`One client ${userId} has disconnected from server (${socket.id}).`);
  });
});
