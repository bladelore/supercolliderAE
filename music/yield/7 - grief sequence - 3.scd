Server.killAll();

s.boot
Pdef.clear;


(
~mainout = 0;
~longverb = Bus.audio(s,2);
~nhverb = Bus.audio(s,2);
~miVerb = Bus.audio(s,2);
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

~convolve_A=Bus.audio(s,2);
~convolve_B=Bus.audio(s,2);

~morph_A=Bus.audio(s,2);
~morph_B=Bus.audio(s,2);

//fx routing
Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\eq, Pmono(\EQstack, \inbus, ~eq, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));

Pdef(\morph,
	Pmono(\cepstralMorph_fx,
		\amp, 1,
		\inbus_A, ~convolve_A,
		\inbus_B, ~convolve_B,
		\addAction, \addToTail,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\nhverb,
	Pmono(\nhverb,
		\amp, 1,
		// \dur, 0.01,
		\inbus, ~nhverb,
		\addAction, \addToTail,
		\gain, 0,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\miVerb,
	Pmono(\miVerb,
		\amp, 1,
		// \dur, 0.01,
		\inbus, ~miVerb,
		\addAction, \addToTail,
		\gain, 0,
		\callback, { Pdefn(\fxid, ~id) },
	)
);
)

(
a = Dictionary();
~analyzeSlices.(
"/Users/aelazary/Desktop/Samples etc./Field recs/Rock song.wav"
	, a, 0.2, \centroid);

b = Dictionary();
~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Money Counting ; Pull Money Bills From Clip, Rapid Counting, And Place Into Pocket. - Counting Money.wav", b, 0.2, \centroid);

c = Dictionary();
~analyzeSlices.(
"/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Rocks Grind And Impact ; Rocks Scraping And Movement. - Scrape Rock.wav"
	, c, 0.2, \centroid);

d = Dictionary();
~analyzeSlices.(
	"/Users/aelazary/Desktop/Samples etc./NEW sample lib/BELLS/Ringing the Peace Bell-F0kitWohMbo.wav"
	, d, 0.1, \centroid);
)


Pdef.clear;

(
Pdef(\sample, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 1,
	\buf, a.at(\file),
	\sliceStart, 0,
	\sliceStutter, 1,
	// \slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a),
	 \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	// \curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 0.5,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.5,
	\pitchRatio, 0.5,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
);
);

Pdef(\sample2, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 1,
	\buf, c.at(\file),
	\sliceStart, 60,
	\sliceStutter, 1,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), c),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	\rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 0.5,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.5,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
);
);

Pdef(\sample3, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 1,
	\buf, b.at(\file),
	\sliceStart, 60,
	\sliceStutter, 1,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), b),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	\rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 0.5,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.5,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
);
);

Pdef(\padDrone,
	Pmono(\padKlank_mono,
		\f, 15,
		\harmonicRatio, 0.9,
		\bw, 1,
		\bwScale, 1,
		\stretch, 1,
		\detuneRate, 800,
		\detuneDepth, 100,
		\impulse, 20000,
		\dev, 1
	)
);

Pdef(\padDrone2,
	Pmono(\padKlank_mono,
		\f, 30,
		\harmonicRatio, 1,
		\bw, 0.1,
		\bwScale, 1,
		\stretch, 0.5,
		\detuneRate, 800,
		\detuneDepth, 0,
		\impulse, 20000,
		\dev, 1
	)
);

Pdef(\padDrone3,
	Pmono(\padKlank_mono,
		\f, 30,
		\harmonicRatio, 1,
		\bw, 1,
		\bwScale, 1,
		\stretch, 1,
		\detuneRate, 800,
		\detuneDepth, 1000,
		\impulse, 20000,
		\dev, 1
	)
);
)

(
~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
)


(
    ~busArr = [
        ~reverb_out=Bus.audio(s,2),
        ~kick_out=Bus.audio(s,2),
        ~padDrone_out=Bus.audio(s,2),
        ~seq1_out=Bus.audio(s,2),
        ~grain_out=Bus.audio(s,2),
        ~vocoder_out=Bus.audio(s,2),
        ~padDrone2_out=Bus.audio(s,2),
    ]
)

(
    ~recorders = ~recordBuses.value(
        ~busArr,
        Platform.recordingsDir +/+ "grief sequence/%.wav"
    );
)

(
	SynthDef(\liveGrain_mono, { |bufnum|
		var polarity, polarityProb, pan, grains, fb;
		var ptr, prev, current;
		//bufFrames
		var bufFrames = BufFrames.ir(bufnum);
		//in
		var feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9).softclip;
		// var in = SoundIn.ar(0);
		var in = InFeedback.ar(\inbus.kr(0), 1);
		//trigger and duration
		var trigRate = \trigRate.kr(100);
		var t = Impulse.ar(trigRate);
		var dur = trigRate.reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;
		//sample bounds
		var startsamp = \startsamp.kr(0) * bufFrames;
		var endsamp = \endsamp.kr(1) * bufFrames;
		var sampsDur = endsamp - startsamp;
		//readback phasor
		var granPhasor = Phasor.ar(
			rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(1),
			start: startsamp,
			end: endsamp,
			//trig: gate,
			resetPos: startsamp
		);
		//overdub input
		ptr = Phasor.ar(0, \recRate.kr(1), 0, bufFrames);
	
		prev = BufRd.ar(1, bufnum, ptr);
		current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
		BufWr.ar(current, bufnum, ptr);
	
		granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);
		//polarity modulation (REDO)
	/*	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
		polarityProb = (polarityProb * 2 - 1);
		polarity = Demand.ar(t, 0, polarityProb);*/
	
		polarity = ~velvet.(t, \density.kr(1), 1 - \polarityMod.kr(1));
	
		pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));
	
		//grain output
		grains = TGrains.ar(
			numChannels: 2,
			trigger: t,
			bufnum: bufnum,
			rate: \rate.kr(1),
			centerPos: (granPhasor * BufDur.kr(bufnum)) + (dur * 0.5001),
			dur: dur,
			pan: pan,
			amp: polarity,
			interp: 4
		);
	
		grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0));
	
		//overdub feedback
		fb = grains.sum * 0.5;
		fb = LeakDC.ar(fb, 0.995);
		LocalOut.ar(fb);
	
		grains = grains * \gain.kr(0).dbamp;
		grains = grains * \amp.kr(1);
		Out.ar(\out.kr(0), grains);
	}).add;
	
	SynthDef(\miVerb, {
		var in, sig;
		in = InFeedback.ar(\inbus.kr(0), 2).sanitize;
	
		sig = MiVerb.ar(
			in,
			time: \time.kr(0.7),
			drywet: \drywet.kr(1),
			damp: \damp.kr(0.5),
			hp: \hp.kr(0.05),
			freeze: \freeze.kr(0),
			diff: \diff.kr(0.625),
		);
	
		sig = sig * \gain.kr(0).dbamp;
		sig = sig * \amp.kr(1);
		sig = sig.sanitize;
		Out.ar(\out.kr(0), sig);
	}).add;
)

(
t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
Pdef(\player,
	Pspawner({| sp |
		var sectionLength = 32;

		Pdef(\p1,
			~makeSubdivision.(
				PlaceAll([[1, 1.5], [1.5, Rest(1.5)], 1, [Rest(0.5), 1]], inf),
				PlaceAll([[4, 1], 4, 0], inf)
			)
		);


		\a.postln;
		//reverb
		sp.par(
			Pbindf(
				Pdef(\miVerb),
				\time, 0.2,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -24,
				\out, [~mainout, ~reverb_out]
			)
		);

		sp.par(
			Pbindf(
				Pdef(\padDrone),
				\out, [~convolve_B, ~mainout, ~padDrone_out]
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\sample)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\rate, 1,
				\sliceStart, 0,
				\sliceStutter, 1,
				\slice, ~pGetSlice.(Pkey(\cyclecount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a),
				\out, [~mainout, ~convolve_A, ~seq1_out]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A, ~grain_out]
		 	).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb, ~vocoder_out]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		///////////////////////////////
		\b.postln;
		sectionLength=64;

		sp.par(
			Pbindf(
				Pdef(\padDrone),
				\out, [~convolve_B, ~grain, ~mainout, ~padDrone_out]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\sample)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\rate, 1,
				\sliceStart, 0,
				\sliceStutter, 1,
				\slice, ~pGetSlice.(Pkey(\cyclecount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a),
				\out, [~mainout, ~convolve_A, ~seq1_out]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\amp, 1,
						\dec, 0.8,
						\freq, 40,
						\drive, -12,
						\dec, Pkey(\bardelta).linlin(0, 1, 0.8, 0.2),
					)
					<> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 4)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1])
					<> Pdef(\p1)
					<> Pbind(\instrument, \simpleSub)
				),
				\out, [~mainout, ~convolve_A, ~miVerb, ~kick_out]
			).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A, ~grain_out]
		 	).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb, ~vocoder_out]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		///////////////////////////////
		\c.postln;
		sectionLength=64;


		Pdef(\p1,
			~makeSubdivision.(
				PlaceAll([[1, 1.5], 1.5, 1, [Rest(0.5), 1]], inf),
				PlaceAll([[4, 1], 4, 0], inf)
			)
		);

		sp.par(
			Pbindf(
				~pFade.(Pdef(\padDrone), Pdef(\padDrone3), 12, curve:'linear', loop: 1),
				\out, [~convolve_B, ~grain, ~mainout, ~padDrone_out]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\sample)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~mainout, ~convolve_A, ~seq1_out]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\amp, 1,
						\dec, 0.8,
						\freq, 40,
						\drive, -12,
						\dec, Pkey(\bardelta).linlin(0, 1, 0.8, 0.2),
					)
					<> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 4)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2])
					<> Pdef(\p1)
					<> Pbind(\instrument, \simpleSub)
				),
				\out, [~mainout, ~convolve_B, ~miVerb, ~kick_out]
			).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A, ~grain_out]
		 	).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb, ~vocoder_out]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);

		///////////////////////////////
		\d.postln;
		sectionLength=96;

		Pdef(\p1,
			~pFade.(
				Pdef(\p1_a,
					~makeSubdivision.(
						PlaceAll([[1, 1.5], 1.5, 1, [Rest(0.5), 1]], inf),
						PlaceAll([[4, 1], 4, 0], inf)
					)
				),

				Pdef(\p1_b,
					~makeSubdivision.(
						PlaceAll([[1.5, Rest(1.5)], 1.5, 1, Rest(1)], inf),
						PlaceAll([[4, 0, 5], [4, 5, 0], [0, 4, 3], [4, 0, 3]], inf)
					)
				),
				16, loop:1)
		);

		sp.par(
			Pbindf(
				~pFade.(Pdef(\padDrone3), Pdef(\padDrone2), 12, curve:'linear', loop: 1),
				\out, [~convolve_B, ~grain, ~mainout, ~padDrone_out]
			).finDur(sectionLength)
		);

		Pdef(\seq1_a,
			Pbind(\sliceStart, 0, \sliceStutter, 1, \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a))
			<>Pdef(\sample)
			<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 5)
			<> Pdef(\p1)
			<> Pbind(\instrument, \segPlayer)
		);

		Pdef(\seq1_b,
			Pbind(\sliceStart, 60, \sliceStutter, 1, \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a))
			<>Pdef(\sample)
			// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 5)
			<> Pdef(\p1)
			<> Pbind(\instrument, \segPlayer)
		);


		sp.par(
			Pbindf(
				~pFade.(Pdef(\seq1_a), Pdef(\seq1_b), 16, curve:'linear', loop: 1),
			\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_B, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					// <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~convolve_A, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\amp, 1,
						\dec, 0.8,
						\freq, 40,
						\drive, -12,
						\dec, Pkey(\bardelta).linlin(0, 1, 0.8, 0.2),
					)
					<> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 4)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2])
					<> Pdef(\p1)
					<> Pbind(\instrument, \simpleSub)
				),
				\out, [~mainout, ~convolve_B, ~miVerb, ~kick_out]
			).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A, ~grain_out]
		 	).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb, ~vocoder_out]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);

		// ///////////////////////////////
		\e.postln;
		sectionLength=48;

		Pdef(\padDrone4,
			Pmono(\padKlank_mono,
				\f, 30,
				\harmonicRatio, 1,
				\bw, 0.1,
				\bwScale, 1,
				\stretch, 0.25,
				\detuneRate, 800,
				\detuneDepth, 1,
				\impulse, 20000,
				\dev, 1
			)
		);

		sp.par(
			PfadeOut(
				Pbindf(
					~pFade.(Pdef(\padDrone3), Pdef(\padDrone2), 12, curve:'linear', loop: 1),
					\out, [~convolve_B, ~grain, ~mainout, ~padDrone_out]
			), 16).finDur(sectionLength)
		);

		sp.par(
			PfadeIn(
				Pbindf(
					~pFade.(Pdef(\padDrone4), Pdef(\padDrone3), 24, curve:'linear', loop: 1),
					\out, [~convolve_B, ~grain, ~mainout, ~padDrone2_out]
			), 8).finDur(sectionLength)
		);

		Pdef(\seq1_a,
			Pbind(\sliceStart, 0, \sliceStutter, 1, \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a))
			<>Pdef(\sample)
			// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 5)
			<> Pdef(\p1)
			<> Pbind(\instrument, \segPlayer)
		);

		Pdef(\seq1_b,
			Pbind(\sliceStart, 60, \sliceStutter, 1, \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a))
			<>Pdef(\sample)
			// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 5)
			<> Pdef(\p1)
			<> Pbind(\instrument, \segPlayer)
		);


		sp.par(
			Pbindf(
				~pFade.(Pdef(\seq1_a), Pdef(\seq1_b), 16, curve:'linear', loop: 1),
				\out, [~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_B, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					// <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~convolve_A, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb, ~vocoder_out]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		///////////////////////////////
		\f.postln;
		sectionLength=96;

		Pdef(\padDrone5,
			Pmono(\padKlank_mono,
				\f, 30,
				\harmonicRatio, 0.5,
				\bw, 0.1,
				\bwScale, 2,
				\stretch, 1,
				\detuneRate, 800,
				\detuneDepth, 0,
				\impulse, 20000,
				\dev, 0
			)
		);

		sp.par(
			PfadeIn(
				Pbind(
					\amp, Pseg(Pseq([0.2, 0.5, 0.2]), 8, 'lin', inf),
					\instrument, \hh,
					\gain, -12,
					\dur, 1/3,
					\out, [~convolve_B]
				), 8
			).finDur(sectionLength)
		);

		Pdef(\p1,
			~pFade.(
				Pdef(\p1_a,
					~makeSubdivision.(
						PlaceAll([[1, 1.5], 1.5, 1, [0.5, 1]], inf),
						PlaceAll([[4, 1], 4, 0], inf)
					)
				),

				Pdef(\p1_b,
					~makeSubdivision.(
						PlaceAll([[1.5, 1.5], 1.5, 1, 1], inf),
						PlaceAll([[4, 0, 5], [4, 5, 0], [0, 4, 3], [4, 0, 3]], inf)
					)
				),
				16, loop:1)
		);

		sp.par(
			Pbindf(
				~pFade.(Pdef(\padDrone5), Pdef(\padDrone4), 8, curve:'linear', loop: 1),
				\out, [~convolve_B, ~grain, ~mainout, ~padDrone_out]
			).trace.finDur(sectionLength)
		);

		Pdef(\seq1_a,
			Pbind(\rate, 0.3, \sliceStart, 0, \sliceStutter, 4, \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a))
			<> Pdef(\sample)
			<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 4)
			<> Pdef(\p1)
			<> Pbind(\instrument, \segPlayer)
		);

		Pdef(\seq1_b,
			Pbind(\rate, 0.3, \sliceStart, 60, \sliceStutter, 4, \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a))
			<>Pdef(\sample)
			<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 4)
			<> Pdef(\p1)
			<> Pbind(\instrument, \segPlayer)
		);

		sp.par(
			Pbindf(
				~pFade.(Pdef(\seq1_a), Pdef(\seq1_b), 8, curve:'linear', loop: 1),
				\out, [~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 4)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_B, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 4)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~convolve_A, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\amp, 1,
						\dec, 0.8,
						\freq, 40,
						\drive, -12,
						\dec, Pkey(\dur)
						)
					// <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 4)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2])
					<> Pdef(\p1)
					<> Pbind(\instrument, \simpleSub)
					),
				\out, [~mainout, ~convolve_B, ~kick_out]
				).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_B, ~grain_out]
		 	).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb, ~vocoder_out]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		///////////////////////////////
		\g.postln;
		sectionLength=48;

		sp.par(
			PfadeOut(
			Pbindf(
				~pFade.(Pdef(\padDrone5), Pdef(\padDrone4), 8, curve:'linear', loop: 1),
				\out, [~convolve_B, ~grain, ~mainout, ~padDrone_out]
			),
				48)
		);
		sp.wait(sectionLength);
		sp.suspendAll;
	})
).play(t);
)