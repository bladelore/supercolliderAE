(

    SynthDef(\additivePerc, {
        var sig, env;
        var atk = \atk.kr(0.01);
        var dec = \dec.kr(1);
        var gate = \gate.kr(1);
        var oneshot  = \oneshot.kr(1);
        var bufs     = \wt.kr(0 ! 4);
        var slice    = \slice.kr(0 ! 2);
        var start    = slice[0];
        var end      = slice[1];
        var rate     = \rate.kr(1);
        var durSamps = (end - start) * bufs[3] * rate;
        var durSecs  = durSamps / SampleRate.ir * rate;
        var phasor   = Phasor.ar(0, 1 / durSamps);
        var line     = Sweep.ar(1, 1 / durSecs).min(1);
        var phase    = Select.ar(oneshot, [phasor, line]);
        var resonator, resFreq, resAtk, resDec;

        var scale = Scale.major.tuning_(\just);

        durSecs = (dec * durSecs).min(2);        

        sig = AdditiveReader(freqBuf: bufs[0], ampBuf: bufs[1], phaseBuf: bufs[2], hopSize: bufs[3], numPartials: 32)
            .readPhase(phase, startFrame: start, endFrame: end)
            .hpFilter(50)
            .lpFilter(\lpf.kr(2000), 4)
            .ampAbove(\ampThresh.kr(0))
            .evenOddMask(\odd.kr(1), \even.kr(1))
            .quantizePartials(scale, \quantize.kr(0.8), 60.midicps)
            .ampSlew(\ampAtk.kr(0), \ampRel.kr(0))
            .ampNormalise(-50, \flatten.kr(0))
            .spectralTilt(\tilt.kr(0))
            .transpose(\transpose.kr(0))
            .limiter
            .oscBank
            .midSideSpread(\spread.kr(1))
            // .ampEnv(\gate.kr(1), atk: atk, rel: durSecs, atkSkew: \atkSkew.kr(0), relSkew: \relSkew.kr(0))
        ;

        sig = sig * 0.01;
        
        // env = Env.perc(attackTime: \atk.kr(0.01), releaseTime: durSecs, level: 1.0);
        env = Env.adsr(attackTime: \atk.kr(0.01), decayTime: durSecs, sustainLevel: \sus.kr(0.5), releaseTime: \rel.kr(0));
        env = EnvGen.kr(env, gate, doneAction: oneshot.linlin(0,1,0,2));
        env = Select.kr(oneshot, [1, env]);
        sig = sig * env;

        //resonator
        resFreq = \resFreq.kr(50);
        resAtk = \resAtk.kr(0.01);
        resDec = \resDec.kr(0.25) + resAtk;
        resonator = Formlet.ar(sig * \resInGain.kr(-30).dbamp, resFreq, resAtk, resDec);
        resonator = (resonator * \resDistort.kr(1)).tanh * \resOutGain.kr(-12).dbamp * \resAmp.kr(1);
        sig = sig + resonator;

        sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        sig = sig.sanitize;

        // DetectSilence.ar(sig, doneAction: 2);
        
        Out.ar(\out.kr(0), sig);
    }).add;
)

(
SynthDef(\segPlayer2, {|buf, slice = #[0, 1]|
	var offset, startsamp, endsamp, sig, numChans, pan, panned, env;
	//slice index
	offset = \offset.kr(0);
	startsamp = slice[0] + offset;
	endsamp = slice[1] + offset;
	//channels and sampler
	// numChans = buf.numChannels;
	numChans = 2;
	// numChans = \stereo.ir(1);
	sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope

    env = Env.adsr(attackTime: \atk.kr(0.01), decayTime: \dec.kr(1), sustainLevel: \sus.kr(0), releaseTime: \rel.kr(0));
	env = EnvGen.ar(env, gate: \gate.kr(1), doneAction: 2);
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

(
a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill Origins/Snowblind/05 drums02.wav");
// a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-BellPlate3.wav");
// a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-Cajon3.wav");
// a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Card Shoe Clicks; Dealer Card Shoe Clicks. - Dealing Cards.wav");
a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-TempleBowls.wav");
// a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-03/FSL-03-Wood Staff Hits, Multiple; Multiple Light Wood Staff Impacts With Handling. - Wood Hits.wav");
AdditiveSines.analyse(s, a, numPartials: 32, windowSize: 2048, slicerThreshold: 0.3, order: 1, detectionThreshold: -90,
    action: { |wt|
        ~wt = wt;
        ~wt.loadBuffers(s);
        "done".postln;
    });
)

(


    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1] * 1, inf),
            PlaceAll([[2, 1], 2, 2], inf)
        )
    );

    Pdef(\additivePerc,
        Pbind(
            \wt, [~wt.asControlInput],
            \instrument, \additivePerc,

            // \dur, Pwrand([1, 0.5, 2, 3], [1, 2, 0.25, 0.125].normalizeSum, inf).stutter(4),
            // \dur, Pseq([0.25, Rest(0.25), 0.5, Rest(0.25), 0.25, 0.25, 0.25, 0.75, 0.5, 0.25], inf),
            // \dur, Pseq([0.25, 0.5, 0.25, 0.25], inf),
            // \dur, 1,
            // \atk, 0.02,
            // \atk, Pwrand([0.1, 0.05], [1, 0.25].normalizeSum, inf),
            \dec, Pwrand([0.1, 1], [0.5, 0.5], inf),
            \sus, Pwrand([0, 0.1], [0.5, 0.5], inf),
            // \sus, 0,
            \rel, 0.5,
            \transpose, 0,
            \oneshot, 1,
            \sliceStart, Pstep([0, 5, 2], 1, inf),
            \ampAtk, 0,
            \ampRel, 0,
            // \ampThresh, Prand([0.2, 0, 0.1], inf),
            \flatten, Pwrand([0, 1], [0.5, 0.5], inf).stutter(2),
            \odd, 1,
            \even, 1,
            \rate, 0.5,
            \lpf, 20000,
            \tilt, 6,
            \quantize, 0,
            \spread, 1,
            \slice, ~wt.pGetSlice(Pseries(0, 1, inf).wrap(0, 16)).stutter(16),
            \resFreq, 60,
            \resDistort, 1,
            \resAmp, 1,
            \resDec, 0.5,
            \gain, 40,
            \amp, 1,
            \pan, Pwhite(-0.5, 0.5,inf),
            \out, ~bus2
        )
        // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 5], reject: 1)
        <> Pdef(\p1)    
    ).play(t);
)