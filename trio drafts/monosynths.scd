
p = PmonoArtic(\sawpulse, \dur, 0.25, \freq, Pwhite(1, 8) * 100, \legato, 1 ).play(t);

p.stop;

LinkClock

l = LinkClock(1).latency_(Server.default.latency);

k.gui;

(
   SynthDef(\escthy,{|gate=1|
        var sig;
        var globalLag = \globalLag.kr(0);
        var pulseWidth = Lag.kr(\pulseWidth.kr(0.5), globalLag);
        var combEnv = Lag.kr(\combEnv.kr(2), globalLag);
        var pitchEnv = Lag.kr(\pitchEnv.kr(0.01), globalLag);

        var pitchMod = Lag.kr(\pitchMod.kr(10), globalLag);
        var baseFreq = Lag.kr(\freq.kr(50), globalLag);
        var pitchNoise = Lag.kr(\pitchNoise.kr(0), globalLag);
        var filter;
        var filterEnv = Lag.kr(\filterEnv.kr(1), globalLag);
        var noiseEnv = \noiseEnv.kr(0.1);
        var shifterRatio = \shifterRatio.kr(1);
        var ratios = Lag.kr(\ratios.kr(1 ! 5), globalLag);

        var filterHi = \filterHi.kr(15000);
        var filterLo = \filterLo.kr(50);

        // sig = Pulse.ar(
        //     (baseFreq * ratios * XLine.ar(pitchMod, 1, pitchEnv)) + (WhiteNoise.ar(1) * 1000 * pitchNoise),
        //     XLine.ar(0, 1, pitchEnv) * (1..5) * pulseWidth
        // );

        sig = SinOscFB.ar(
            (baseFreq * ratios * XLine.ar(pitchMod, 1, pitchEnv)) + (WhiteNoise.ar(1) * 1000 * pitchNoise),
            (1..5) * pulseWidth * pi
        );

        filterEnv = EnvGen.ar(Env(levels: [filterLo, filterHi, filterLo], times: (filterEnv) ! 3, curve: 8), gate: 0.5);
        // filterEnv.scope;
        // //
        sig = CombC.ar(sig, 0.01, XLine.kr(0.0001, 1, combEnv / (1..5)), 0.5);
        // // sig = sig.tanh;
        sig = HPF.ar(sig, Lag.ar(filterEnv, globalLag));
        sig = RLPF.ar(sig, Lag.ar(filterEnv, globalLag), 1);
                
        // sig = sig + PinkNoise.ar(noiseEnv);
        sig = VASEM12.ar(sig, XLine.ar(5000, 50, pitchEnv), res: 1, transition: SinOsc.ar(0.1 * (1..5)));
        sig = VASEM12.ar(sig, XLine.ar(50, 5000, pitchEnv), res: 1, transition: Line.ar(1, 0, filterEnv));
        
        sig = sig + (PitchShift.ar(sig, pitchRatio: [1, 1.5, 2] * shifterRatio, windowSize: \shifterWindow.kr(0.2), timeDispersion: 0.001, pitchDispersion: 0.05).sum * \shifterMix.kr(1));
        
        sig = Disperser.ar(
            input: sig,
            freq: baseFreq * ratios,
            resonance: \resonance.kr(0.5),
            mix: \disperserMix.kr(1),
            feedback: 1
        );

        sig = sig * EnvGen.ar(Env.adsr(\atk.kr(0), 0.2, 0.5, \rel.kr(0.1)), gate, doneAction: 2);

        sig = Splay.ar(sig, 1);

        sig = Rotate2.ar(sig[0], sig[1], LFSaw.ar(\rotate.kr(0.01)));

        sig = HPF.ar(sig, \hpf_out.kr(50));

        sig = sig.tanh;
        sig = sig * \amp.kr(1);
        sig = sig * \gain.kr(1).dbamp;
        sig = sig.sanitize;

        Out.ar(\out.kr(0), sig);
    }).add;
)



(

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([2, 4, 2, 2, 4], inf)
        )
    );

    Pdef(\scytheParams,
        PmonoArtic(
            
            \escthy,
            \amp, 1,
            // \dur, 0.25,
            \legato, Prand([0.5, 1], inf),
            \globalLag, ~slider.(0),
            // \gain, -24,
            // \amp, ~slider.(0),
            \atk, ~slider.(1).linlin(0, 1, 0, 2),
            \rel, ~slider.(2).linlin(0, 1, 0.1, 4),
            \freq, Pseq([47, 47, 47].midicps, inf),
            // \pulseWidth, Pwhite(-0.99, 0.99, inf),
            // \shifterRatio, 1,

            \ratios, [[1, 1, 1, 1, 1]] * 0.5,
            // \ratios, [[1.005, 1.001, 1.01, 1.001, 1.02]],
            // \ratios, [[2, 3, 7, 5, 1.5]] * 0.25,
            // \ratios, [[1.0, 1.5, 1.75, 2.25, 5]] * 3,

            // \ratios, Pseq([[1, 1, 1, 1, 1]] * 0.5, [[2, 3, 7, 5, 1.5]], inf).stutter(4),

            \filterEnv, ~slider.(4).linexp(0,1,0.01, 1) - 0.01,
            \combEnv, ~slider.(5).linexp(0, 1, 0.001, 2),
            \pitchEnv, ~slider.(6).linexp(0, 1, 0.01, 4),
            \pitchNoise, ~slider.(7).linexp(0,1,0.01,1) - 0.01,

            \shifterMix, ~knob.(0),
            \shifterWindow, ~knob.(1),
            \disperserMix, ~knob.(2),
            \pulseWidth, ~knob.(3).linlin(0,1,0.01,0.99),
            \resonance, ~knob.(4),
            \rotate, ~knob.(5).linexp(0, 1, 0.01, 15) - 0.01,
            \filterLo, 50,
            \filterHi, 20000,
            \out, ~bus1,
        )

        <> Pdef(\p1)
    );
)

~fm7SynthNames = 32.collect{|algoNum| "fm7algo%".format(algoNum).asSymbol};

(
~fm7SynthNames.do{|synthName, algo|
	SynthDef(synthName, {|out=0, dur=0.01, sustain=0, freqScale=0.5, spread=0.25, pan=0, feedback=1.0, amp=0.5|
		var env = Env.perc.kr(gate:1, timeScale: dur * (1+sustain), doneAction: Done.freeSelf);
		var ctls =
		[
			// freq, phase, amp
			[freqScale * 300, pi.rand, 1],
			[freqScale * 2500, pi.rand, 1],
			[freqScale * SinOsc.ar(Rand(0.1,10.0)).exprange(3,100), 0, 1],
			[freqScale * LFNoise2.ar(10).exprange(1300,0.5), 0, 1],
			[freqScale * ExpRand(30,1500), pi.rand, 1],
			[freqScale * ExpRand(30,500), pi.rand, 1]
		];

		var sig = FM7.arAlgo(algo, ctls , feedback * 2.0);
		sig = Splay.ar(sig, spread: spread, center: pan);
		sig = sig * env * amp;

		Out.ar(out, sig);
	}).add;
};
)
(
    SynthDef(\eightoheightsub,{|gate=0.5|
        var sig, globalLag, freq, pitchEnv, atk, dec, sus, rel, sweep, drive;

        globalLag = \globalLag.kr(0);
        freq = Lag.kr(\freq.kr(60), globalLag);
        atk = Lag.kr(\atk.kr(0.05), globalLag);
        dec = Lag.kr(\dec.kr(0.2), globalLag);
        sus = Lag.kr(\sus.kr(0), globalLag);
        rel = Lag.kr(\rel.kr(0), globalLag);
        drive = Lag.kr(\drive.kr(10).neg, globalLag);
        sweep = Lag.kr(\sweep.kr(1.5), globalLag);

        pitchEnv = 1 + (
            sweep * 
            Env.perc(0.0, 0.13, curve: -4).ar * 
            XLine.ar(1, 0.6, dec)
        );

        sig = SinOsc.ar(freq * pitchEnv);
        sig = sig * EnvGen.ar(Env.adsr(atk, dec, sus, rel), gate, doneAction: 2);

        sig = SelectX.ar(\driveMix.kr(1), [sig, (sig * drive.neg.dbamp).tanh * drive.abs.dbamp]);

        sig = Compander.ar(sig, sig,
            thresh: 0.01,
            slopeAbove: 0.3,
            clampTime: 0.002,
            relaxTime: 0.1,
        );

        sig = sig * \gain.kr(0).dbamp;
        sig = Pan2.ar(sig, \pan.kr(0));
        sig = LeakDC.ar(in: sig, coef: 0.995);
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;


    Pdef(\subParams,
        PmonoArtic(
            \eightoheightsub,
            \amp, 1,
            // \dur, 0.75, 
            \dur, Pseq([0.5, 0.5, Rest(0.5)], inf),
            \dur, Pseq([0.5, 0.75, 0.75] * 2, inf),
            //legato controls if notes are connected
            \legato, Prand([0.5, 1], inf),
            //use globalLag 
            \globalLag, 1,
            \freq, Pseq(([31, 30, 30, 30] - 8).midicps, inf),
            \sweep, 1,
            \drive, 0,
            \driveMix, 1,
            // \drive, Pseq([20, 10], inf),
            // \atk, Pseq([0.2, 0.5], inf),
            \atk, 0.3,
            \dec, 1,
            \sus, 1,
            \rel, 0.4,
            \gain, 20,
            \out, ~bus2
        )
    );
)

(
    Pdef(\subParams).play(t);
    Pdef(\scytheParams).play(t)
)

// create a wavetable (or use your own)
(
)

DualOscOS

~buffer

BufRd

.collect

(
    var file;

    //file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/effectronImpResp.wav";
    
    // file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/ep0ImpResp.wav");
    // file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/ep0ImpResp.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./analogprincess + [chw] - coldwinter kit/hardstyle/kix - 1.wav";
    file = "/Users/aelazary/Desktop/Samples etc./analogprincess + [chw] - coldwinter kit/percs & fx/fx - warx.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Underpasses/FremontTroll.wav";

    {
        var cyclePos, phase, sig, currentSample, fb, fbIdx, size, freq;

        fb = 1;
        size = 512;
        ~buffer = 2.collect({|i| Buffer.readChannel(s, file, channels:[i], numFrames: -1);});
        freq=4;
        // cyclePos = SinOsc.ar(0.1, 2pi).linlin(-1, 1, 0, 1);
        cyclePos = LFNoise2.ar(0.1).linlin(-1, 1, 0, 1);
        // cyclePos = Phasor.ar(DC.ar(0), 4);
        phase = Phasor.ar(DC.ar(0), freq * SampleDur.ir);

        ~buffer.collect({|buf, i|
            sig = SingleOscOS.ar(
                bufnum: buf,
                phase: phase,
                numCycles: BufFrames.kr(buf) / size,
                cyclePos: cyclePos,
                oversample: 4,
            );
            
            fbIdx = cyclePos * BufFrames.kr(buf);
            currentSample = BufRd.ar(1, buf, fbIdx);
            currentSample = ((sig * fb) + currentSample);
                // .clip(0, 1);
            currentSample = LPF.ar(currentSample, 15000);
            if(fb > 0){ BufWr.ar(LeakDC.ar(currentSample), buf, fbIdx);};
            sig = sig.tanh();
            LeakDC.ar(sig) * \gain.kr(-6).dbamp;
        });
        
    }.play;
)

Shaper

(

    var file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/effectronImpResp.wav";
    
    // ~buffer = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/ep0ImpResp.wav");
    // ~buffer = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav");
    ~buffer = 2.collect({|i| Buffer.readChannel(s, file, channels:[i]);});

    t = Signal.sineFill(2048, [1], [0]);
    u = Signal.sineFill(2048, 1.0/((1..512)**2)*([1,0,-1,0]!128).flatten);
    w = Signal.sineFill(2048, 1.0/(1..512)*([1,0]!256).flatten);
    x = Signal.sineFill(2048, 1.0/(1..512));
    v = t.addAll(u).addAll(w).addAll(x);

    ~buffer = Buffer.loadCollection(s, v);

    {
        var cyclePos, phase, sig, currentSample, fb, fbIdx, size, freq;

        fb = 1;
        size = 2048;
        freq =  60 * SampleDur.ir;

        // cyclePos = SinOsc.ar(0.1, 2pi).linlin(-1, 1, 0, 1);
        cyclePos = LFNoise2.ar(1.5).linlin(-1, 1, 0, 1);
        phase = Phasor.ar(DC.ar(0), freq);

        sig = SingleOscOS.ar(
            bufnum: ~buffer,
            phase: phase,
            numCycles: BufFrames.kr(~buffer) / size,
            cyclePos: cyclePos,
            oversample: 1,
        );
        
        fbIdx = cyclePos * BufFrames.kr(~buffer);
        currentSample = BufRd.ar(1, ~buffer, fbIdx);
        currentSample = (sig * fb) + currentSample;
	    BufWr.ar(currentSample, ~buffer, fbIdx);

        sig = LeakDC.ar(sig) * 0.1;

        sig ! 2;
    }.play;
)



~buffer.numFrames

(
    var path, soundFile, size, skipTime, freq;
    path = Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff";
    skipTime = 1.2589;
    freq = 132.5;
    size = 2048;

    soundFile = SoundFile.new(path);
    soundFile.openRead;

    x = Signal.readWave(path, size, (soundFile.sampleRate * skipTime).asInteger, freq: freq);
    x.plot("waveform: %".format(path), minval: -1.0, maxval: 1.0);

    soundFile.close;
)

(
var size = 2048;
var rfftsize = (size / 2 + 1).asInteger;
var rcosTable = Signal.rfftCosTable(rfftsize);
var sampleRate = 44100;
var freq1 = sampleRate * 2.pow(-2);
var freq0 = freq1 * 2.pow(-3);  // 3 octaves

// var chirp = Signal.cosineChirp(size, freq0, freq1, sampleRate: sampleRate);  // white
var chirp = Signal.cosineChirp(size, freq0, freq1, beta: -1, sampleRate: sampleRate);  // pink
~buffer = Buffer.loadCollection(s, chirp);

// chirp.plot;
// chirp.rfft(rcosTable).magnitude.ampdb.plot
)