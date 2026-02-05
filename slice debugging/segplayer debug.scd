(
SynthDef(\segPlayer, {|bufnum|
	var pan = \pan.kr(0);
	//slice index
	//var this_idx = \slice.kr(0);
	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = Select.kr(this_idx, ~onsetArr);
	var endsamp = Select.kr(this_idx + 1, ~onsetArr);
	//channels and sampler
	var numChans = bufnum.numChannels;
	var sig = PlayBuf.ar(numChans, bufnum, BufRateScale.ir(bufnum) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	var panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	var env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction:2);
	//out
	sig = panned * env * \gain.kr(0).dbamp;

	sig = SelectX.ar(\pitchMix.kr(0), [sig,
		PitchShift.ar(sig,
			pitchRatio: \pitchRatio.kr(1),
			windowSize: \windowSize.kr(0.01),
			pitchDispersion: \pitchDispersion.kr(1),
			timeDispersion: \timeDispersion.kr(0.1))]
	);
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\segPlayer_new, {|buf, slice = #[0, 1]|
	var offset, startsamp, endsamp, numChans, sig, pan, panned, env;
	//slice index
	offset = \offset.kr(0);
	startsamp = slice[0] + offset;
	endsamp = slice[1] + offset;
	//channels and sampler
	numChans = buf.numChannels;
	sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction: 2);
	//out
	sig = panned * env * \gain.kr(0).dbamp;

	sig = SelectX.ar(\pitchMix.kr(0), [sig,
	PitchShift.ar(sig,
	pitchRatio: \pitchRatio.kr(1),
	windowSize: \windowSize.kr(0.01),
	pitchDispersion: \pitchDispersion.kr(1),
	timeDispersion: \timeDispersion.kr(0.1))]
	);

	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;
)

Synth(\segPlayer_new, [\buf, a.at(\file), slice: ~getSlice.(69, a, 0)]);
Synth(\segPlayer, [\buf, ~src, slice: 69]);


(
Pdef(\segPattern, Pbind(
	\amp, 1,
	\bufnum, ~src.bufnum,
	\dec, Pkey(\dur) * 1,
	\sliceStart, 68,
	\slice, (Pseries(0, 1, inf).wrap(0, 32).stutter(4) + Pkey(\sliceStart)).trace,
	\pan, ~pmodenv.(Pseq([-0.3, 0 ,0.3],inf), Pkey(\dec), 1, \linear),
	\atk, 0.001,
	\rel, 0.4,
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, -20), Pkey(\dec), 1),
	\rate, 1,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.7,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 1, 0.01), Pkey(\dec), 1, \sine),
	\timeDispersion, 0.01,
	)
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4])
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
	<> ~makeSubdivision.(
		Pseq([2, 2, 1, 1], 1),
		Pseq(#[2, 2, 4, 2, 2, 4], 1)
	)
	<> Pbind(\instrument, \segPlayer)
).play(t);
)

(
Pdef(\segPattern, Pbind(
	\amp, 1,
	\buf, a.at(\file),
	\dec, Pkey(\dur) * 1,
	\sliceStart, 68,
	\slice, ~pGetSlice.(i, a, Pseries(0, 1, inf).wrap(0, 32).stutter(4) + Pkey(\sliceStart)).trace,
	\pan, ~pmodenv.(Pseq([-0.3, 0 ,0.3],inf), Pkey(\dec), 1, \linear),
	//\pan, 0,
	\atk, 0.001,
	\rel, 0.4,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 0.4, -4), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, -20), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.7,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 1, 0.01), Pkey(\dec), 1, \sine),
	\timeDispersion, 0.01,
	)
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4])
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25))
	<> ~makeSubdivision.(
		Pseq([2, 2, 1, 1], 1),
		Pseq(#[2, 2, 4, 2, 2, 4], 1)
	)
	<> Pbind(\instrument, \segPlayer_new)
).play(t);
)

