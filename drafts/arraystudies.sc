(
SynthDef(\counter, {
    var size, arr, ptr, val, rate;
    rate = 10;
    size = 10;
    arr = (0..(size-1)).as(LocalBuf);
    // ptr = PulseCount.ar(Impulse.ar(rate)) - 1;  // Fixed syntax
    // ptr = ptr.wrap(0, size);

    //skips first value if not initialised
    ptr = Stepper.ar(Impulse.ar(rate), Impulse.kr(0), 0, size-1, 1, 0);
    val = BufRd.ar(1, arr, phase: ptr, interpolation: 1);
    val.poll(rate);
}).add;
)

10

Synth(\counter)

IndexL


// Everyone likes Fibonacci numbers:
b = Buffer.loadCollection(s, [1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89] * 0.1);
// Or you could load some numbers from a file:
b = Buffer.loadCollection(s, FileReader.read("/Users/danstowell/svn/stored_docs/bbx annots/onsets_gt/vb5gt.txt", true, true).collect(_.at(0).asFloat));
(
// ListTrig used here to output some simple grains.
// I'm also using .poll and a ramp to output the calculated time value, to check the output.
// Note the accuracy, which is limited to the accuracy of the control rate.
x = { |t_reset=0|
    var trigs, env, son, ramp;
    trigs = ListTrig.kr(b.bufnum, t_reset);
    env = EnvGen.ar(Env.perc(0.01, 0.1), trigs);
    son = SinOsc.ar(440, 0, env * 0.2);
    
    ramp = Phasor.kr(t_reset, ControlRate.ir.reciprocal, 0, inf);
    ramp.poll(trigs, "Trigger at time offset");
    
    son.dup;
}.play(s)
);
x.set(\t_reset, 1);

b = Buffer.alloc(s, 100);
(
// This example simply stores values regularly sampled from an oscillator.
// With such a small buffer, it doesn't take long to fill up.
// Note what happens when full.
x = {
var source;
source = LFCub.kr(10, 0, EnvGen.kr(Env.linen(1, 2, 1), doneAction:2));
Logger.kr(source, Impulse.kr(49), b.bufnum);
}.play(s);
)
x.free;
b.plot;

b = Buffer.alloc(s, 100, 3);
(
// The same but multi-channel.
x = {
var source;
source = LFCub.kr(10, 0, EnvGen.kr(Env.linen(1, 2, 1), doneAction:2));
Logger.kr([source, source * 0.5, source + WhiteNoise.kr(0.3)], Impulse.kr(49), b.bufnum);
}.play(s);
)
x.free;
b.plot;

b = Buffer.alloc(s, 100);
(
// This time we'll trigger something to create and store random values, and recall them later.
x = { |t_trig=0, t_reset=0|
var source;
source = LFNoise0.kr(10);
source.poll(t_trig, "Storing this random value");
Logger.kr(source, t_trig, b.bufnum, t_reset);
}.play(s);
)

x.set(\t_trig, 1); // Call this a few times
b.loadToFloatArray(action:{|ar| ar.postcs}) // Dump the values
x.set(\t_reset, 1); // When you want to start from scratch