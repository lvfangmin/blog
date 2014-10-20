Title: Ways to Inject Mock Modules into Nodejs Testcases
Author: Allan Lv
Date: Mon Oct 20 20:22:45 CST 2014
Categories: nodejs, mock, testcase

Sometimes it is just plain hard to test our projects because it depends on other modules that cannot be used in the test enviroment. This could be because they aren't available, they will not return the results needed for the test or because executing them would have undesirable side effects, for example 'cost a lot of time'. In other cases, our test strategy requires us to have more control or visibility of the internal behavior.

When we're writing a test in which we can't use a real depended-on module, we can replace it with a mock one. The mocked one doesn't have to behave exactly like the real module, it merely has to provide the same API as the real one.

After we implemented the mocked module, we need to inject the modules into target module. This Artical is going to introduce three ways to inject the modules into NodeJS projects, also will explain the benifit and defect of each way.

Terminology in this article:

- target module: the module we want to test on
- mock module: the modules we used to mock the real dependencies

## Hijack


From the document of Nodejs we know that:

> Modules are cached after the first time they are loaded. This means (among other things) that every call to require('foo') will get exactly the same object returned, if it would resolve to the same file.

	// directly set the cached module before load our test module
	require.cache.http  = {exports: httpMock};
    require.cache.https = {exports: httpMock};

    var targetModule = require('target-module.js');

So we can `use the cache mechanism` to mock the modules, Load the mocked module to require.cache before running the test, which will cause the test module using the mocked module.

- Benifit:
  - Easy to mock.
- Defect:
  - Need to clear the mock env after test done or will effect the following tests.


## Sandbox

Load the module through our own sandbox, in the sandbox we can replace the global functions, such as `require`, so we can load the mocked module.

	/**
	 * Briefly explain the work flow of this function
	 * @param {String} test module path.
	 * @param {Object=} mocked modules
	 */
	function loadmodule (modulepath, mock) {
		// our own context used to load modules
		context.require = function(name) {
        	return mock[name] || mock[name + '.js'] || require_(name);
    	};

    	// read the module
    	var script = fs.readFileSync(modulepath);

    	// add set function to script to mock this module's private variables and functions
    	script += '\n exports.set = function(name, value) { eval(name + " = value"); };';

    	// run the script on our own context, which will using the mock modules
    	vm.runInNewContext(script, context);

    	return context;
    }

This is the concise code of the `loadmodule`, in the real implementaion we can add `cache` and `global context` to this function to make it work the same as the NodeJS Load Modules mechanism.


- Benifit:
  - Easy to mock.
  - Can export private variables and functions to test.
  - Can mock private variables and functions to easy the test.
  - Using seperate context in each testcase, so do not need to clear env after test done.
- Defect:
  - `vm is not quite stable currently`, such as '[] instnaceof Array' will return false in our own sandbox, we can come around this problem by using 'Array.isArray([])' currently. Details on seem on [Nodejs VM](http://nodejs.org/docs/latest/api/all.html#all_executing_javascript).

## Mock Without Using Sandbox
Use NodeJS's own require mechanism to load the modules, add `set` and `get` function to the target module.

The pseudocode:

	function loadmodule(modulepath) {
	  	// create a testmodule as it would be created by require()
		var Module = require('module');
		var targetModule = new Module(modulepath, Module.parent);

		var set = function (name, value) { eval (name + ' = value'); };

		Module.wrapper = Module.wrapper + '\nmodule.exports.set = ' + set.toString() + ';';

		// load modules using the original NodeJS way
		targetModule.load(targetModule.id);

		return targetModule;
	}

	usage:
	var targetModule = loadmodule('./target_module.js');

	targetModule.set('fs', {
		readFile: function(path, encoding, cb) {
			cb(null, 'Success!');
		}
	});

- Benifit:
  - Same as `Sandbox mechanism`
  - Don't need to emulate the NodeJS's module environment.
- Defect:
  - A bit more complex to implement, not so straightforward as the second `Sandbox` solution.

I've not implemented this yet, if you're interested about this solution, please visit jhnns's github project [NodeJS rewire module](https://github.com/jhnns/rewire).


## End

It's really interesting to investigate how to make the test easier, currently I prefer the `Sandbox` and `Mock without using sandbox` solution, since it is more powerful and flexiable. If you have other ways to inject mocked modules, please share will me.
