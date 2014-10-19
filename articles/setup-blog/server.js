// Just a basic server setup for this site
var Stack = require('stack'),
    Creationix = require('creationix'),
    Http = require('http');

Http.createServer(Stack(
  Creationix.log(),
  // NOTE: Need to change to the blog.git dir
  require('wheat')(__dirname +"/../blog.git")
)).listen(80);
