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
io.adapter(RedisAdapter());

app.get('/', function (req, res) {
  res.sendfile(__dirname + '/index.html');
});

io.on('connection', (socket) => {
  const {userId} = socket.handshake.query;

  logger.info(`One client ${userId} has connected to server (${socket.id}).`);

  socket.on('message', function (socket) {
    socket.broadcast.emit('user connected');
  });
});
