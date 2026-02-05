Pdef.clear;

t = TempoClock.new(120/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

FluidWaveform(~src, ~indices);

(
// var file = "C:/Users/Asher/Desktop/Samples etc/Field recs/Rock song.wav";
// var file = "C:/Users/Asher/Desktop/Samples etc/Field recs/Brush metal bowl.wav";
// var file = "C:/Users/Asher/Desktop/Samples etc/Field recs/Candle roll on table.wav";
var file = "C:/Users/Asher/Desktop/Samples etc/Field recs/Big bowl flick.wav";
// var file = "C:/Users/Asher/Desktop/Samples etc/Field recs/Bell birds.wav";
//analyse mono
~src = Buffer.readChannel(s, file, channels: [0]);
//make indices buffer
~indices = Buffer(s);
~meanfeatures = Buffer(s);
~spec = Buffer(s);
~stats = Buffer(s);
//nrt onset slice of source
FluidBufOnsetSlice.processBlocking(s, ~src, metric: 9, threshold: 0.2, indices: ~indices, action: {"found slices".postln});
//FluidBufNoveltySlice.processBlocking(s, ~src, indices: ~indices, action: {"found slices".postln});
)

(
~indices.loadToFloatArray(action: {
	arg fa;
	//iterate through adjacent pairs of indices (tuple like)
	fa.doAdjacentPairs{
		arg start, end, i;
		var numSamps = end - start;
		//compute spectral features per fft frame (centroid only)
		FluidBufSpectralShape.processBlocking(s, ~src, start, numSamps, features: ~spec, select:[\centroid]);
		//buf stats channels: mean std skew kurtosis min median max
		//select mean
		FluidBufStats.processBlocking(s, ~spec, stats: ~stats, select:[\mean]);
		FluidBufCompose.processBlocking(s, ~stats, destination: ~meanfeatures, destStartFrame: i);
	};
	//get indices
	~onsetArr = fa;
	//get selected features
	~meanfeatures.loadToFloatArray(action: { arg fa; ~meanfeatures = fa;});
	//sort selected feature
	~featuresSorted = ~meanfeatures.order;
	"done analysis".postln;
});
)

~meanfeatures

(
~mainout = 0;
~longverb = Bus.audio(s,2);
~longverb2 = Bus.audio(s,2);
~delay = Bus.audio(s,2);
~chorus = Bus.audio(s,2);

~modDelay=Bus.audio(s,2);
~mod1_in = Bus.audio(s, 2);
~mod1_out = Bus.control(s, 1);

~modal=Bus.audio(s,2);
~formant=Bus.audio(s,2);
~grain=Bus.audio(s,2);
~tape=Bus.audio(s,2);
~pitchShift=Bus.audio(s,2);
~filter=Bus.audio(s,2);
~eq=Bus.audio(s,2);

~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
)

(
//fx routing
Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\eq, Pmono(\EQstack, \inbus, ~eq, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));

)

(
Pdef(\seq1,
	Pbind(
		\amp, 1,
		\dec, Pkey(\dur) * 1,
		\sustainTime, Pkey(\dur) * 0.01,
		\atk, 0.05,
		\rel, Pkey(\dur) * 5,
		\pan, Pwhite(-0.5, 0.5),
		\f0, 90,
		\structure, 2.5,
		\brightness, Pkey(\groupdelta).linlin(0, 1, 0.5, 0.75),
		\damping, Pkey(\groupdelta),
		\accent, 0.5,
		\harmonicstretch, 0.75,
		\position, Pkey(\cycledelta),
		\loss, Pwhite(0.1, 1),
		\modeNum, 3,
		\cosFreq, ~pmodenv.(Pwhite(0.1, 1), Pkey(\dur)),
	)
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 4])
	// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1))
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\cycledelta))
	// <> Pbind(\timingOffset, Pkey(\cyclecount) / Pkey(\groupdelta))
	<> ~makeSubdivision.(
		Pseq([1.5, 1, 1, 1, 0.5], 1),
		Pseq([4, 4, 5], 1)
	)
	<> Pbind(\instrument, \rongsinator)
);

Pdef(\seq2,
	Pbind(
		\amp, 1,
		\dec, Pkey(\dur) * 1,
		\sustainTime, Pkey(\dur) * 0.01,
		\atk, 0.05,
		\rel, Pkey(\dur) * 2,
		\pan, Pwhite(-0.5, 0.5),
		\f0, 180,
		\structure, 0.75,
		\brightness, Pkey(\groupdelta).linlin(0, 1, 0.5, 0.75),
		\damping, Pkey(\groupdelta),
		\accent, 0.5,
		\harmonicstretch, 0.9,
		\position, Pkey(\cycledelta),
		\loss, Pwhite(0.1, 1),
		\cosFreq, Pwhite(0.1, 1),
	)
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 4])
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	// <> Pbind(\timingOffset, Pkey(\cyclecount) / Pkey(\groupdelta))
	<> ~makeSubdivision.(
		Pseq([1.5, 1, 1, 1, 0.5], 1),
		Pseq([4, 4, 5], 1)
	)
	<> Pbind(\instrument, \rongsinator)
);

Pdef(\kickDrone,
	Pbind(
		\amp, 1,
		\freq, 50,
		\atk, 1,
		\dec, 2,
		\fb, 1,
		\ratio, 4,
		\index, 0.25,
		\gain, -20,
	)
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1.5, 1.5, 1.5], 1),
		Pseq([3, 3, 4], 1)
	)
	<> Pbind(\instrument, \fmKick)
);

Pdef(\segPattern, Pbind(
	\amp, 1,
	\bufnum, ~src,
	\dec, Pkey(\dur) * 1,
	\sliceStart, 0,
	\slice, (Pseries(0, 1, inf).wrap(0, 32).stutter(3) + Pkey(\sliceStart)).trace,
	\pan, ~pmodenv.(Pseq([-0.3, 0 ,0.3],inf), Pkey(\dec), 1, \linear),
	//\pan, 0,
	\atk, 0.001,
	\rel, 4,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 0.4, 0), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 1, 0.01), Pkey(\dec), 1, \sine),
	\timeDispersion, 0.01,
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 4])
<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
<> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25))
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5], 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \segPlayer)
).play(t);
)


{AnalogSnareDrum.ar(trig: Trig.ar(1 ! 2, dur:0.001), infsustain: 0, accent: 1, freq: 50, tone: 1, decay: 0.1, snappy: 1)}.play;

(
Pdef(\player,
	Pspawner({| sp |
		////////////////////////////////////////////////////////
		\a.postln;
		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\seq1),
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					\f0, 90,
					\out, [~eq],
					\gain, 6,
				), 8, 0.5
			).finDur(64)
		);

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\out, [~mainout]
			).finDur(64)
		);


/*		sp.par(
			PfadeIn(
				Pbindf(
					Pdef(\kickDrone),
					// \index, Pseg(Pseq([1, 1.5]), 64),
					\out, [~mainout, ~grain]
			), 32).finDur(64)
		);*/


		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~filter, ~mod1_in]
			).finDur(64)
		);

		sp.par(
			Pdef(\follower1,
				Pmono(\envfollow,
					\dur, 0.1,
					\atk, 0.01,
					\rel, 0.01,
					\amp, 0.1,
					\mul, 1,
					\add, 1,
					\inbus, ~mod1_in,
					\out, ~mod1_out,
				)
			).finDur(64)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 800],inf), 4, 'lin' , inf),
				\res, 0.5,
				\out, [~mainout, ~grain]
			).finDur(64)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 100,
				\trigRate, 500,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0,
				\feedback, 0,
				\gain, -20,
				\out, [~mainout]
			).finDur(64)
		);

		sp.wait(64);
		////////////////////////////////////////////////////////
		\b.postln;

		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\seq1),
					\f0, 180,
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					\out, [~eq],
					\gain, 6,
				), 8, 0.5
			).finDur(64)
		);

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\out, [~mainout],
				\rate, 0.75
			).finDur(64)
		);

		sp.par(
			Pbindf(
				Pdef(\kickDrone),
				\index, Pseg(Pseq([1, 1.5]), 64),
				// \stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
				\out, [~mainout, ~grain]
			).finDur(64)
		);


		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~mainout, ~grain]
			).finDur(64)
		);

		//FX
		/*		sp.par(
		Pbindf(
		Pdef(\grain),
		\bufnum, ~bufA,
		\overlap, 100,
		\trigRate, 500,
		\posRate, 0.9,
		\rate, 1,
		\recRate, 1,
		\polarityMod, 1,
		\overdub, 0.4,
		\feedback, 0.3,
		\gain, -18,
		\out, [~mainout]
		).finDur(64)
		);*/

		sp.wait(64);
		////////////////////////////////////////////////////////
		\b.postln;

	})
).play(t);
)
