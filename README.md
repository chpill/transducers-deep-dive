# transducers-deep-dive

Just a bit of code to play with transducers. Just launch a `boot repl` and off you go!

Or better: `boot cider repl -s watch refresh`



#### [transducers_deep_dive.batch](https://github.com/chpill/transducers-deep-dive/blob/master/src/transducers_deep_dive/batch.clj)

See accompanying blog post https://medium.com/@chpill_/deep-dive-into-a-clojure-transducer-3d4117784fa6 


#### [transducers_deep_dive.stop_when](https://github.com/chpill/transducers-deep-dive/blob/master/src/transducers_deep_dive/stop_when.clj)

Creating a `stop-when` transducer akin to `halt-when`, but that does not
sidetrack the downstream transducers on interruption.


#### [transducers_deep_dive.async](https://github.com/chpill/transducers-deep-dive/blob/master/src/transducers_deep_dive/stop_when.clj)

Just following along on code examples from a talk on core.async.
