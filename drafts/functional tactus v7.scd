(
//Server.default.options.device = "headphonesSc";
s.boot;
)
s.quit;

(
SynthDef(\kick, {
	var snd, partialDecays;
	snd = SinOsc.ar(\freq.kr(30) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1) * [1, 1.3, 5.4, 4.8]);
	snd = snd * [0, -10, -5, -8].dbamp;
	partialDecays = (3..0).lincurve(0, 3, 0.01, 3, 5);
	snd = snd * Env.perc(0, partialDecays * \partials.kr(1)).ar;
	snd = snd.sum;
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	//snd = ((snd * -6.dbamp)).tanh;
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(2), curve: -3), \gate.ar(1), doneAction: Done.freeSelf);
	snd = snd * \gain.kr(-15).dbamp;
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\kick2, {
	var snd;
	snd = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1));
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	//snd = ((snd * 10.dbamp)).tanh;
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(2), curve: -3), \gate.ar(1) , doneAction: Done.freeSelf);
	snd = snd * \gain.kr(-15).dbamp;
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = LeakDC.ar(in: snd, coef: 0.995);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\snare, {
	var snd, noiseArr;
	snd = SinOsc.ar(\freq.kr(200) * (1 + (2 * Env.perc(0.001, 0.03).ar)) * ([1, 1.3, 2.5, 5.8]));
	snd = snd * Env.perc(0, [2, 0.7, 0.7, 0.8]).ar;
	//snd = snd * [0, -20, -12, -24].dbamp;
	snd = snd.sum;
	snd = snd + (SinOsc.ar(XLine.ar(20000, 100, 0.03)) * Env.perc(0.01, 0.03).ar * -3.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 2120, 1.2) * Env.perc(0.05, 0.1).ar * -3.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 9120, 2) * Env.perc(0.003, 0.15).ar * -8.dbamp);
	snd = snd * Env.perc(0.001, 0.8, curve: -4).ar;
	//verb
//	snd = snd + (GVerb.ar(snd, \roomsize.kr(1.3), \reverbtime.kr(3)) * -20.dbamp);
/*	(1..3).do{ |i|
		snd = snd + LPF.ar(PitchShift.ar(snd, i.linlin(1, 3, 0.001, 0.003), i.linexp(1,3,1.4,2.6)) * -20.dbamp, 4000);
	};*/

	//snd = snd + ((snd * 20.dbamp).tanh * -2.dbamp);

	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.001), \dec.kr(0.8), curve: -4), \gate.ar(1), doneAction: Done.freeSelf);
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = snd* \gain.kr(-25).dbamp;
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\hh, { |gate=1|
	var snd;
	snd = WhiteNoise.ar([0.8, 0.7]);
	snd = BPF.ar(snd, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	snd = CombL.ar(snd, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	//snd = snd + (GVerb.ar(snd, 4, 30));
	snd = snd *EnvGen.ar(Env.perc(\atk.kr(0.001), \dec.kr(0.2), curve: -4), gate, doneAction: Done.freeSelf);
	snd = snd * \gain.kr(-15).dbamp;
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;

// A simple delay
SynthDef(\delay, {
	var sig, feed;

	feed = -6.dbamp * LocalIn.ar(2);
	feed = LeakDC.ar(feed);
	feed = DelayC.ar(feed, 4, [0.5/4, 1/4]);
	feed = feed - FreqShift.ar(feed, SinOsc.kr([1, 1.1]).range(0, 0.15));
	feed = Rotate2.ar(feed[0], feed[1], LFSaw.kr(0.1));

	sig = In.ar(\in.kr, 2);

	LocalOut.ar(
		DelayN.ar(sig, 1, [2/4, 1/4]) + feed
	);

	sig = sig + feed;

	Out.ar(\out.kr(0), sig);
}).add;
)

//t.schedAbs(t.nextBar, {t.beatsPerBar_(4)});
t.schedAbs(t.nextBar, {t.tempo_(170/60)});

///COMPOSTION FUNCTIONS
(
//CLOCK
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

//to do
//metric modulation
//accel/decel by phase?
//swing/swing by group
//interpolate between patterns by over x bars
//choke groups
//add risers to hits
//sequence sections
//delta to

~makeSubdivision = { |patt, subdiv|
	var groupCycle = subdiv.asStream.all.size;


	patt = Pn(patt, inf);
	subdiv = subdiv.asInteger;
	subdiv = Pdup(subdiv, subdiv);

	//calculate delta values from subdivision pattern
	Plazy{ ~makeDeltas.(~pattern) } <>
	Pbind(
		\subdiv, Pn(subdiv , inf),
		\dur, Psubdivide(Pkey(\subdiv), patt),
		\groupCycle, Pn(groupCycle, inf),
	)
};

~makeDeltas = { |patt|
		var eventcount = 0;
		var groupcount = 0;
		//var groupIdxCount
		Pbind(
			//delta time in relation to tempo clock
			\bardelta, (Ptime() % t.beatsPerBar) / t.beatsPerBar,
			//janky ass subroutine
			//group delta time
			\groupdelta, Pfunc({|event|
				var groupdelta;
				//event index in cycle
				event[\eventcount] = eventcount + 1;
				eventcount = eventcount + 1;
				//as delta within group
				groupdelta = (eventcount - 1) / event[\subdiv];
				//index of group
				if(groupcount == event[\groupCycle]) {
					groupcount = 0;
				};
				//set current group count at key
				event[\groupcount] = groupcount + 1;
				//reset event and increment group count
				if(eventcount == event[\subdiv] ) {
					eventcount = 0;
					groupcount = groupcount + 1;
				};
				groupdelta;
			}),
		)
};

~nearestDelta = { |patt, key, downbeat, centre|
	//wtf does this do tbh
	abs(round(centre - key, downbeat) - (centre - downbeat));
};

~filterBeat = { |patt, key, beat|
	var thiskey = key.key;
	Pfunc({ |event| if(beat.includes(event[thiskey])) { event } { event[\instrument] = \rest }});
};
)

// Define a PSeq
//PATTERNS
(
Pdef(\player).stop;

~p = Pseq(#[1, 1, 2], 1);
~s = Pseq(#[5, 5, 5], 1);
//
//\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60))

Pdef(\kickMod, Pbind(
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.75, -1),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1),
	)
);

Pdef(\kickPattern1,
	PmonoArtic(\kick2) <>
	Pdef(\kickMod) <>
	Pbind(\stretch, Pkey(\bardelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4]) <>
	~makeSubdivision.(~p, ~s)
);

Pdef(\kickPattern2,
	PmonoArtic(\kick) <>
	Pbind(\stretch, Pkey(\groupdelta).linexp(0, 1, 0.25, 2)) <>
	Pdef(\kickMod) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4]) <>
	~makeSubdivision.(
		Pseq(#[3, 1.5, 1, 1.5, 4], 1),
		Pseq(#[3, 4, 4, 2], 1)
	)
);
)

Pdef(\kickPattern1).finDur(16).play(t, quant:[1,0])
Pdef(\kickPattern2).finDur(16).play(t, quant:[1,0])


/*(
//~next = false;
Ndef.clear;
Ndef(\fx).proxyspace.quant = 1.0;
Ndef(\fx).fadeTime = 0;
Ndef(\fx).clock = t;
Ndef(\fx)[999]=Pdef(\kickPattern).finDur(16);
//Ndef(\fx).filter(2, {|in| PitchShift.ar(in,pitchRatio:rrand(0.4,1))});
Ndef(\fx).play;
)*/
//Pfx(Pdef(\kickPattern1),\delay).play(t, quant:2);

(
Pdef(\player,
	Ppar([
		Pseq([
			Pdef(\kickPattern1).finDur(4),
			Pdef(\kickPattern2).finDur(5),
			]
		)
	])
).play(t, quant:[1,0]);
)
