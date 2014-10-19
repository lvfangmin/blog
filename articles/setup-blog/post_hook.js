var http = require('http');

http.createServer(function (req, res) {
    console.log( 'Post-receive form Github');

    // Execute a git fetch on the bare repo.
    // NOTE: Need to change to the blog.git directory
    var gitRepoPath = __dirname +"/../blog.git";
    var gitCommand = "git --git-dir " + gitRepoPath + " fetch origin master:master";

    var sys = require('sys')
    var exec = require('child_process').exec;
    var fetchOutput = exec(gitCommand, function puts(error, stdout, stderr) {
       if (error) {
           console.log('Error occured \n[' + error+']');
       }
    });

    fetchOutput.on('exit', function (code) {
        console.log('Child process exited with exit code '+code);
    });
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end("");
}).listen(7000);

console.log('Github hook running at http://127.0.0.1:7000/');
