// boot with extended resources

(
s.options.numWireBufs = 64 * 16;
s.options.memSize = 8192 * 64;
s.reboot;
)

(
~maxNumChannels  = 30;
~inBus = Bus.audio(s, ~maxNumChannels);
~maxDelay = 0.2;
)


(
SynthDef(\granulator_1, {
    arg outBus = 0, overallAtt = 0.01, overallRel = 0.1, grainDur = 0.1,
		pulseFreq = 10, numChannels = 10, channelOffset = 0,
		pos = 0, posDev = 0, delayRange = 5,
		gate = 1, preAmp = 1, limitAmp = 0.1;

	var maxGrainDur = ~maxNumChannels / pulseFreq - 0.001;
	var maxDelay = 20;
	var grainEnv = Env.sine(min(maxGrainDur, grainDur));

    var trig = Impulse.ar(pulseFreq);

	// multichannel trigger
    var trigs = { |i|
		PulseDivider.ar(trig, ~maxNumChannels, ~maxNumChannels - 1 - i)
	} ! ~maxNumChannels;

	// multichannel envelope for Grains (multichannel expansion because of trigs)
    var grainEnvGens = EnvGen.ar(grainEnv, trigs);

	// global env
	var envGen = EnvGen.ar(Env.asr(overallAtt, 1, overallRel), 1, doneAction: 2);

	// multichannel in signal is wrapped if numChannels < maxNumChannels
	var in = { |i|
		Select.ar(i % numChannels + channelOffset % ~maxNumChannels, In.ar(~inBus, ~maxNumChannels))
	} ! ~maxNumChannels;
	var grains = in * grainEnvGens * envGen * preAmp;
	var sig;

	// core workhorse, each grain gets own position
	// array of stereo signals
	sig = { |i|
		var localPos = (pos.lag(1) + TRand.ar(posDev.neg, posDev, trigs[i])).clip(-1, 1);
		var localDelay = LFDNoise3.ar(0.1).range(0, delayRange.lag(1) * 0.001);
		Pan2.ar(DelayL.ar(grains[i], ~maxDelay, localDelay), localPos)
	} ! ~maxNumChannels;

	// mix to stereo
	Out.ar(0, Limiter.ar(Mix(sig), limitAmp))

}, metadata: (
	specs: (
		grainDur: [0.001, 0.2, 3, 0, 0.015],
		pulseFreq: [1, 500, 3, 0, 10],
		pos: [-1, 1, \lin, 0, 0],
		posDev: [0, 0.2, \lin, 0, 0.25],
		delayRange: [0, 10, 2, 0, 1],
		numChannels: [1, ~maxNumChannels, \lin, 1, ~maxNumChannels],
		channelOffset: [0, ~maxNumChannels - 1, \lin, 1, 0],
		preAmp: [0, 3, \lin, 0, 0.5],
		limitAmp: [0, 0.5, 2, 0, 0.1]
	)
)
).add;
)

(
SynthDescLib.global[\granulator_1].makeGui;
s.scope;
s.freqscope;
)

// start gui


// only white noise

x = { Out.ar(~inBus, WhiteNoise.ar(0.1 ! ~maxNumChannels)) }.play



// granulator can continue

x.free



// 2 synths play on sub-busses

(
~n = ~maxNumChannels / 2;
x = { Out.ar(~inBus.subBus(0, ~n), WhiteNoise.ar(0.1 ! ~n)) }.play
)

y = { Out.ar(~inBus.subBus(~n, ~n), Saw.ar({ exprand(150, 1500) } ! ~n)) }.play

x.free

y.free



// saw with fixed frequencies

x = { Out.ar(~inBus, Saw.ar({ exprand(100, 3000) } ! ~maxNumChannels, 0.1)) }.play

x.free


// glisson synthesis

(
x = {
	Out.ar(
		~inBus,
		Saw.ar(
			LFDNoise3.ar({ rrand(0.1, 5) } ! ~maxNumChannels).exprange(150, 5000),
			mul: 0.1
		)
	)
}.play
)

x.free



// mixed noise

(
x = {
	var n = ~maxNumChannels.div(3);
	Out.ar(
		~inBus,
		[
			WhiteNoise.ar(0.1 ! n),
			Impulse.ar({ rrand(1000, 3000) } ! n),
			ClipNoise.ar(0.1 ! n)
		].flop.flat
	)
}.play
)

x.free


// mixed sources

(
x = {
	var n = ~maxNumChannels.div(5);
	Out.ar(
		~inBus,
		[
			SinOsc.ar(LFDNoise3.ar(0.3 ! n).range(300, 1500), 0, 0.3),
			WhiteNoise.ar(0.1 ! n),
			Saw.ar(LFDNoise3.ar(0.2 ! n).range(300, 1500), 0.2),
			Crackle.ar(1.9 ! n),
			Pulse.ar(LFDNoise3.ar(0.4 ! n).range(300, 1500), 0.5, 0.2)
		].flop.flat
	)
}.play
)

x.free



// mixed sources, omit channels of multichannel bus

(
x = {
	// only 10 of 30
	var n = 2;
	Out.ar(
		~inBus,
		[
			WhiteNoise.ar(0.1 ! n),
			SinOsc.ar(LFDNoise0.ar(2.1 ! n).range(300, 3000)),
			ClipNoise.ar(0.1 ! n),
			Saw.ar(LFDNoise0.ar(3 ! n).range(300, 3000)),
			Crackle.ar(1.9 ! n),
		].flop.flat
	)
}.play
)

x.free


// read a soundfile

(
p = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
b = Buffer.read(s, p);
)


// multichannel playback, shifted playback rates

(
x = {
	Out.ar(
		~inBus,
		PlayBuf.ar(
			1,
			b,
			((1..~maxNumChannels) / (~maxNumChannels * 10) + 1) * BufRateScale.kr(b),
			loop: 1
		)
	)
}.play
)

x.free

// stop granulator in GUI