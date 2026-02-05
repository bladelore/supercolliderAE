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
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(2), curve: -3), \gate.ar(1), doneAction: Done.freeSelf);
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
	snd = snd *EnvGen.ar(Env.perc(\atk.kr(0.001), \dec.kr(0.2), curve: -4),gate, doneAction: Done.freeSelf);
	snd = snd * \gain.kr(-15).dbamp;
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
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
//	var cycleLength = groupCycle.sum;

	patt = Pn(patt, inf);
	subdiv = subdiv.asInteger;
	subdiv = Pdup(subdiv, subdiv);

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
	//Pfunc({ |event| if(event[thiskey] == beat) { event } { event[\instrument] = \rest }});
	//.select({|event| event[\instrument] != \rest})
};
)

// Define a PSeq
//PATTERNS
(
Pdef(\player).stop;

~p = Pseq(#[1.5, 2, 3, 4], 1);
~s = Pseq(#[3, 3, 4, 4, 2], 1);

//~p = Pseq(#[3, 1.5, 1, 1.5, 4], 1);
//~s = Pseq(#[3, 4, 4, 2], 1);

Pdef(\rhythm_kick, ~makeSubdivision.(~p, ~s));
Pdefn(\rhythm_hh, ~makeSubdivision.(~p, Pseq(#[3, 6, 12], 1)));

Pdef(\getDelta_kick, Plazy{~makeDeltas.(~pattern)});
Pdef(\groupselect_kick, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1]));

Pdef(\atkMod, Pbind(\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.75, -1).linexp(0, 1, 0.01, 0.5)));
Pdef(\freqMod, Pbind(\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80)));
Pdef(\gainMod, Pbind(\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb));
Pdef(\decayMod, Pbind(\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2))).clip(0.01, 1);
Pdef(\sweepMod, Pbind(\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1)));
//
Pdefn(\freqMod_hh, Pbind(\freq, Pkey(\bardelta).linexp(0, 1, 30, 20000)));
Pdefn(\legatoMod_hh, Pbind(\legato, Pkey(\groupdelta).linlin(0, 1, 0.1, 0.8)));
Pdefn(\groupselect_hh, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2,3]));

Pdefn(\stretchMod, Pbind(\stretch, Pkey(\bardelta).linexp(0, 1, 0.125, 4)));
Pdefn(\tempoMod, Pbind(\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60)));

(
Pdef(\kickPattern,
	PmonoArtic(\kick2) <>
	Pdef(\sweepMod) <>
	Pdef(\decayMod) <>
	//Pdefn(\stretchMod) <>
	Pdef(\gainMod) <>
	Pdef(\freqMod) <>
	//Pdef(\groupselect_kick) <>
	Pdef(\atkMod) <>
	Plazy{~makeDeltas.(~pattern)} <>
	~makeSubdivision.(~p, ~s)
	//<>Pdefn(\kickParams, Pbind(\instrument, \kick2, \gain, 0));
);
//).play(t, quant:[1,0]);

Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	Pdefn(\stretchMod) <>
	//Pdefn(\decayMod) <>
	Pdefn(\freqMod_hh) <>
	Pdefn(\legatoMod_hh) <>
	Pdefn(\groupselect_hh) <>
	Pdefn(\getDeltas_hh, ~makeDeltas.(~deltapattern)) <>
	Pdefn(\rhythm_hh)
	//<>Pdefn(\hhParams, Pbind(\instrument, \hh, \gain, -20));
);
//).play(t, quant:[1,0]);

Pdef(\snarePattern,
	Pdefn(\freqMod) <>
	Pdefn(\decayMod) <>
	Pdefn(\getDeltas_snare, ~makeDeltas.(~deltapattern)) <>
	Pdefn(\rhythm_snare, ~makeSubdivision.(~p, Pseq(#[1, 2, 4], 1))) <>
	Pdefn(\groupselect_snare, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2, 4]));
	Pbind(
	\instrument, \snare,
	\dur, Pseq([4, 4, 4, 1, 3], inf, 1),
	\dec, Pseq([0.5, 0.5, 0.5, 1], inf, 0.5) * 0.5,
	\freq, 30,
	\gain, -13,
	)
);

)
//end patterns
)

//change with pdefn redefs -- why does this only work with pmono
Pdef(\kickPattern).finDur(16).trace.play(t, quant:[1,0]);
Pdef(\kickPattern2, Pdef(\kickPattern) <> ~makeDeltas.(~deltapattern) <> ~makeSubdivision.(Pseq(#[2, 0.5, 1, 1], 1), Pseq(#[6, 4, 3], 1)));
Pdef(\kickPattern2).finDur(8).trace.play(t, quant:[1,0]);
Pdef(\kickPattern3, Pdef(\kickPattern) <> Pdefn(\rhythm_kick, ~makeSubdivision.(~p, Pseq(#[6, 4, 3], 1))));

(
Pdef(\player,
	Ppar([
	Pseq([
    	Pdef(\kickPattern).finDur(8),
    	Pdef(\kickPattern2).finDur(8),
    	Pdef(\kickPattern3).finDur(8),]
	)
	])
).play(t, quant:[1,0]);
)
