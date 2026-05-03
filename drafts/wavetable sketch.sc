
DualOscOS

(
    var table, file;

    //file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/effectronImpResp.wav";
    
    // file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/ep0ImpResp.wav");
    // file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/ep0ImpResp.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./analogprincess + [chw] - coldwinter kit/hardstyle/kix - 1.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./scythes/Peening Scythe With Hammer and Anvil.wav";
    file = "/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav";
    // file = "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Underpasses/FremontTroll.wav";
    ~buffer = 2.collect({|i| Buffer.readChannel(s, file, channels:[i], numFrames: -1);});


    // t = Signal.sineFill(2048, [1], [0]);
    // u = Signal.sineFill(2048, 1.0/((1..512)**2)*([1,0,-1,0]!128).flatten);
    // w = Signal.sineFill(2048, 1.0/(1..512)*([1,0]!256).flatten);
    // x = Signal.sineFill(2048, 1.0/(1..512));
    // v = t.addAll(u).addAll(w).addAll(x);

    // // Pulse-width variations
    // v = Signal.sineFill(2048, 1.0/(1..512) * ([1,1,1,0]!128).flatten);
    // y = Signal.sineFill(2048, 1.0/(1..512) * ([1,1,1,1,1,0,0,0]!64).flatten);

    // // // Harmonic decay curves
    // z = Signal.sineFill(2048, (1..512).reciprocal ** 1.5);
    // a = Signal.sineFill(2048, (1..512).reciprocal ** 0.5);

    // // // Odd harmonics with exponential decay
    // b = Signal.sineFill(2048, 1.0/(1,3..512));
    // c = Signal.sineFill(2048, 1.0/((1,3..512) ** 1.3));

    // // // Prime number harmonics
    // d = Signal.sineFill(2048, Signal.newClear(512).collect({|x,i| [2,3,5,7,11,13,17,19,23,29,31,37,41,43,47].includes(i+1).asInteger / (i+1).max(1)}));

    // // // Alternating phase
    // // e = Signal.sineFill(2048, 1.0/(1..512), (0,pi!256).flatten);
    // f = Signal.sineFill(2048, 1.0/(1..512), {|i| [0,pi][i%2]}!512);

    // // // Formant-like spectra
    // g = Signal.sineFill(2048, {|i| exp(-0.01 * ((i-12)**2))}!512); // peak at 12th harmonic
    // h = Signal.sineFill(2048, {|i| exp(-0.005 * ((i-8)**2)) + (exp(-0.01 * ((i-24)**2)) * 0.5)}!512);

    // // // Fibonacci ratios
    // i = Signal.sineFill(2048, 1.0/[1,1,2,3,5,8,13,21,34,55,89,144,233,377]);

    // // Metallic (inharmonic hint via phase)
    // j = Signal.sineFill(2048, 1.0/(1..512) * {1.0.rand}!512, {2pi.rand}!512);

    // table = i;

    // ~buffer = 2.collect({|i| 
    //     Buffer.loadCollection(s, table);}    
    // );

    {
        var cyclePos, phase, sig, currentSample, fb, fbIdx, size, freq, stereoOffset;

        fb = \fb.kr(0);
        size = 128;
        freq = 1 * SampleDur.ir;
        stereoOffset = 1;
        // freq=SinOsc.ar(0.1, 2pi).linlin(-1, 1, 4, 32);
        // cyclePos = SinOsc.ar(LFNoise2.ar(20).linlin(-1, 1, 0, 1), 2pi).linlin(-1, 1, 0, 1);
        // phase = LFNoise2.ar(20).linlin(-1, 1, 0, 1);
        cyclePos = Phasor.ar(DC.ar(0), 1);
        phase = Phasor.ar(DC.ar(0), freq);

        ~buffer.collect({|buf, channel|
            sig = SingleOscOS.ar(
                bufnum: buf,
                phase: phase,
                numCycles: BufFrames.kr(buf) / size,
                cyclePos: (cyclePos + channel.linlin(0,1,0,stereoOffset)).wrap(0,1),
                // cyclePos: cyclePos,
                oversample: 4,
            );
            
            fbIdx = cyclePos * BufFrames.kr(buf);
            currentSample = BufRd.ar(1, buf, fbIdx);
            currentSample = ((sig * fb) + currentSample);
            currentSample = LPF.ar(currentSample, 15000);
            fbIdx = Select.ar(fb > 0, [DC.ar(-1), fbIdx]);
            // if(fb > 0){ BufWr.ar(LeakDC.ar(currentSample), buf, fbIdx);};
            BufWr.ar(LeakDC.ar(currentSample), buf, fbIdx);
            sig = sig.tanh();
            LeakDC.ar(sig) * \gain.kr(-6).dbamp;
        });
        
    }.play;
)

Buffer.freeAll


(
var file;
file = "/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav";
~buffer = 2.collect({|i| Buffer.readChannel(s, file, channels:[i], numFrames: 4096);});

// t = Signal.sineFill(2048, [1], [0]);
// u = Signal.sineFill(2048, 1.0/((1..512)**2)*([1,0,-1,0]!128).flatten);
// w = Signal.sineFill(2048, 1.0/(1..512)*([1,0]!256).flatten);
// x = Signal.sineFill(2048, 1.0/(1..512));
// v = t.addAll(u).addAll(w).addAll(x);
// ~buffer = Buffer.loadCollection(s, v);
// ~buffer = 2.collect({|i| ~buffer = Buffer.loadCollection(s, v);});

Ndef(\fbWt_single,
    {|buffer=#[0,1]|
        var sig, currentSample, fbIdx;

        var globalLag = \globalLag.kr(0);
        
        var size = \size.kr(32);
        var fb = Lag.kr(\fb.kr(0), globalLag);
        var freq = Lag.kr(\freq.kr(60) * SampleDur.ir, globalLag);
        var cycleRate = Lag.kr(\cycleRate.kr(0.1), globalLag);
        // var cyclePos = SinOsc.ar(cycleRate, 2pi).linlin(-1, 1, 0, 1);
        var cyclePos = LFNoise2.ar(0.6).linlin(-1, 1, 0, 1);
        var phase = Phasor.ar(DC.ar(0), freq);
        var stereoOffset = Lag.kr(\stereoOffset.kr(0), globalLag);

        buffer.collect({|buf, channel|
            sig = SingleOscOS.ar(
                bufnum: buf,
                phase: phase,
                numCycles: BufFrames.kr(buf) / size,
                cyclePos: (cyclePos + channel.linlin(0,1,0,stereoOffset)).wrap(0,1),
                // cyclePos: cyclePos,
                oversample: 4,
            );
            
            fbIdx = cyclePos * BufFrames.kr(buf);
            currentSample = BufRd.ar(1, buf, fbIdx);
            currentSample = ((sig * fb) + currentSample);
            currentSample = LPF.ar(currentSample, 15000);
            fbIdx = Select.ar(fb > 0, [DC.ar(-1), fbIdx]);
            BufWr.ar(LeakDC.ar(currentSample), buf, fbIdx);
            sig = sig.tanh();
            LeakDC.ar(sig) * \gain.kr(-6).dbamp;
        });        
    });

    Ndef(\fbWt_single).set(\buffer, ~buffer).play;

    Ndef(\fbWt_single)

)

(
var fileA, fileB;
fileA = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";
fileB = "/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav";
~bufferA = 2.collect({|i| Buffer.readChannel(s, fileA, channels:[i], numFrames: 4096);});
// ~bufferA = 2.collect({|i| Buffer.loadCollection(s, Signal.sineFill(4096, [1]))});
// ~bufferB = 2.collect({|i| Buffer.readChannel(s, fileB, channels:[i], numFrames: 4096);});

Ndef(\fbWt_double,
    {|bufferA=#[0,1], bufferB=#[0,1]|
        var sig, currentSample, fbIdx;
        var globalLag = \globalLag.kr(0);
        var size = \size.kr(4096);
        var fb = Lag.kr(\fb.kr(0), globalLag);
        var freq = Lag.kr(\freq.kr(120) * SampleDur.ir, globalLag);
        // var cyclePosA = LFNoise2.ar(\cyclePosA.kr(0.6)).linlin(-1, 1, 0, 1);
        // var cyclePosB = LFNoise2.ar(\cyclePosB.kr(0.1)).linlin(-1, 1, 0, 1);
        var cyclePosA = SinOsc.ar(\cyclePosA.kr(0.02), 2pi).linlin(-1, 1, 0, 1);
        var cyclePosB = SinOsc.ar(\cyclePosB.kr(0.01), 2pi).linlin(-1, 1, 0, 1);
        var phase = Phasor.ar(DC.ar(0), freq);
        var stereoOffset = Lag.kr(\stereoOffset.kr(1), globalLag);

        sig = bufferA.collect({|bufA, channel|
            var bufB = bufferB[channel];
            sig = DualOscOS.ar(
                bufnumA: bufA,
                phaseA: phase,
                numCyclesA: BufFrames.kr(bufA) / size,
                cyclePosA: (cyclePosA + channel.linlin(0,1,0,stereoOffset * 2pi)).wrap(0,1),

                bufnumB: bufA,
                phaseB: phase,
                numCyclesB: BufFrames.kr(bufA) / size,
                cyclePosB: (cyclePosB + channel.linlin(0,1,0,stereoOffset * 2pi)).wrap(0,1),

                pmIndexA: \pmIndexA.kr(5),
                pmIndexB: \pmIndexB.kr(2),
                pmFilterRatioA: \pmFltRatioA.kr(2),
                pmFilterRatioB: \pmFltRatioB.kr(0.5),
                oversample: 4,
            );
            sig = sig.tanh();
            LeakDC.ar(sig) * \gain.kr(-6).dbamp;
        });

        sig = [sig[0].sum, sig[1].sum];
    }
);

Ndef(\fbWt_double).set(\bufferA, ~bufferA, \bufferB, ~bufferB).play;

)

~test.()

Compander

~test.()

~bufferA

SoundFile.openRead("/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/effectronImpResp.wav").numChannels

(
    var file, table;

    t.tempo = 160/60;

    ~getBuffers = {|file, frames|
        SoundFile.openRead(file).numChannels.collect({|i| Buffer.readChannel(s, file, channels:[i], numFrames: -1);});
    };

    // file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/effectronImpResp.wav";
    file = "/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav";
    // file = "/Users/aelazary/Desktop/Samples etc./scythes/Sharpen the scythe.wav";
    file = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";

    ~bufferA = ~getBuffers.("/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav", -1);
    ~bufferB = ~getBuffers.("/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav", -1);


    SynthDef(\fbWt_single, {|buffer=#[0,1]|

        var sig, currentSample, fbIdx;

        var globalLag = \globalLag.kr(0);

        var atk = Lag.kr(\atk.kr(0.05), globalLag);
        var dec = Lag.kr(\dec.kr(0.2), globalLag);
        var sus = Lag.kr(\sus.kr(0), globalLag);
        var rel = Lag.kr(\rel.kr(0), globalLag);

        var size = Lag.kr(\size.kr(128), globalLag);
        var fb = Lag.kr(\fb.kr(0), globalLag);

        var sweep = Lag.kr(\sweep.kr(1.5), globalLag);

        var pitchEnv = 1 + (
            sweep * 
            Env.perc(0.0, 0.13, curve: -4).ar * 
            XLine.ar(1, 0.6, dec)
        );

        var freq = Lag.kr(\freq.kr(1) * SampleDur.ir, globalLag);
        var cycleRate = Lag.kr(\cycleRate.kr(0.1), globalLag);
        // var cyclePos = SinOsc.ar(cycleRate, 2pi).linlin(-1, 1, 0, 1);
        var cyclePos = LFNoise2.ar(cycleRate).linlin(-1, 1, 0, 1);
        var phase = Phasor.ar(DC.ar(0), freq);
        var cycleOffset = Lag.kr(\cycleOffset.kr(0), globalLag);
        var stereoOffset = Lag.kr(\stereoOffset.kr(0.1), globalLag);

        freq = freq * pitchEnv;

        sig = buffer.collect({|buf, channel|
            sig = SingleOscOS.ar(
                bufnum: buf,
                phase: phase,
                numCycles: BufFrames.kr(buf) / size,
                cyclePos: (cyclePos + cycleOffset + channel.linlin(0, 1, 0, stereoOffset * 2pi)).wrap(0,1),
                // cyclePos: cyclePos,
                oversample: 4,
            );
            
            fbIdx = cyclePos * BufFrames.kr(buf);
            currentSample = BufRd.ar(1, buf, fbIdx);
            currentSample = ((sig * fb) + currentSample);
            // currentSample = LPF.ar(currentSample, 15000);
            fbIdx = Select.ar(fb > 0, [DC.ar(-1), fbIdx]);
            BufWr.ar(LeakDC.ar(currentSample), buf, fbIdx);
            sig = sig.tanh();
            LeakDC.ar(sig) * \gain.kr(-6).dbamp;
        });

        sig = sig * EnvGen.ar(Env.adsr(atk, dec, sus, rel), \gate.kr(0.5), doneAction: 2);

        sig = BRF.ar(sig, \lpf.kr(500), 2);

        Out.ar(\bus.kr(0), sig);
    }).add;

    // Pdef(\table,
    //     PmonoArtic(
    //         \fbWt_single,
    //         // \buffer, Pstep(Pseq([[~bufferA], [~bufferB]], inf), 2, inf),
    //         \buffer, [~bufferB],
    //         \amp, 1,
    //         \fb, 0,
    //         \stereoOffset, 1,
    //         // \dur, Pseq([0.5, 0.5, Rest(0.5)], inf),
    //         //legato controls if notes are connected
    //         \legato, Prand([0.5, 1], inf),
    //         //use globalLag
    //         \globalLag, 1,
    //         // \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf) * 0.25,
    //         // \freq, 60,
    //         \freq, 10,
    //         \sweep, 16,
    //         // \size, Prand([1024, 4096, 2048], inf ),
    //         // \size, 4096,
    //         \size, 13,
    //         // \cycleOffset, 2,
    //         // \cycleRate, Pseq([0.2, 0.5, 2] * 1, inf),

    //         \dur, Prand([2, Rest(2)], inf),
    //         \atk, 1,
    //         \dec, 0.8,
    //         \sus, 1,
    //         \rel, 2,
    //         \lpf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),

    //         // \dur, 0.25,
    //         // \atk, 0.1,
    //         // \dec, 0.2,
    //         // \sus, 0,
    //         // \rel, 0,

    //         \gain, 10,
    //         \out, ~bus2
    //     )
    // ).play(t);

    // Pdef(\table,
    //     PmonoArtic(
    //         \fbWt_single,
    //         // \buffer, Pstep(Pseq([[~bufferA], [~bufferB]], inf), 2, inf),
    //         \buffer, [~bufferA],
    //         \amp, 1,
    //         \fb, 0,
    //         \stereoOffset, 1,
    //         // \dur, Pseq([0.5, 0.5, Rest(0.5)], inf),
    //         //legato controls if notes are connected
    //         \legato, Prand([0.5, 1], inf),
    //         //use globalLag
    //         \globalLag, 16,
    //         // \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf) * 0.25,
    //         // \freq, 60,
    //         \freq, 10,
    //         \sweep, 16,
    //         \size, Prand([1024, 4096, 2048], inf ),
    //         // \size, 4096,
    //         // \size, 13,
    //         // \cycleOffset, 2,
    //         // \cycleRate, Pseq([0.2, 0.5, 2] * 1, inf),

    //         // \dur, Prand([2, Rest(2)], inf),
    //         \dur, 2,
    //         \atk, 1,
    //         \dec, 0.8,
    //         \sus, 1,
    //         \rel, 2,
    //         \lpf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),

    //         // \dur, 0.25,
    //         // \atk, 0.1,
    //         // \dec, 0.2,
    //         // \sus, 0,
    //         // \rel, 0,

    //         \gain, 10,
    //         \out, ~bus2
    //     )
    // ).play(t);

    // Pdef(\table,
    //     PmonoArtic(
    //         \fbWt_single,
    //         // \buffer, Pstep(Pseq([[~bufferA], [~bufferB]], inf), 2, inf),
    //         \buffer, [~bufferA],
    //         \amp, 1,
    //         \fb, 0,
    //         \stereoOffset, PmodEnv(Pexprand(0.01, 1, inf), 1, \sine, inf),
    //         // \dur, Pseq([0.5, 0.5, Rest(0.5)], inf),
    //         //legato controls if notes are connected
    //         \legato, Prand([0.5, 1], inf),
    //         //use globalLag
    //         \globalLag, 16,
    //         // \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf) * 0.25,
    //         \freq, 30,
    //         // \freq, 10,
    //         \sweep, 16,
    //         // \size, Prand([1024, 4096, 2048], inf ),
    //         \size, \512,
    //         // \size, 4096,
    //         // \size, 13,
    //         // \cycleOffset, 2,
    //         // \cycleRate, Pseq([0.2, 0.5, 2] * 1, inf),

    //         \dur, Prand([2, Rest(2)], inf),
    //         // \dur, 2,
    //         \atk, 1,
    //         \dec, 0.8,
    //         \sus, 1,
    //         \rel, 2,
    //         \lpf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),

    //         // \dur, 0.25,
    //         // \atk, 0.1,
    //         // \dec, 0.2,
    //         // \sus, 0,
    //         // \rel, 0,

    //         \gain, 10,
    //         \out, ~bus2
    //     )
    // ).play(t);

    Pdef(\table,
        PmonoArtic(
            \fbWt_single,
            // \buffer, Pstep(Pseq([[~bufferA], [~bufferB]], inf), 2, inf),
            \buffer, [~bufferA],
            \amp, 1,
            \fb, 0,
            \stereoOffset, 1,
            \legato, Prand([0.5, 1], inf),
            \globalLag, 16,
            \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf),
            // \freq, 30,
            // \freq, 10,
            \sweep, 16,
            // \size, Prand([1024, 4096, 2048], inf ),
            \size, \512,
            // \size, 4096,
            // \size, 13,
            // \cycleOffset, 2,
            // \cycleRate, Pseq([0.2, 0.5, 2] * 1, inf),

            \dur, Prand([2, Rest(2)], inf),
            // \dur, 2,
            \atk, 1,
            \dec, 0.8,
            \sus, 1,
            \rel, 2,
            \lpf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),

            // \dur, 0.25,
            // \atk, 0.1,
            // \dec, 0.2,
            // \sus, 0,
            // \rel, 0,

            \gain, 10,
            \out, ~bus2
        )
    ).play(t);

    Pdef(\table,
        PmonoArtic(
            \fbWt_single,
            // \buffer, Pstep(Pseq([[~bufferA], [~bufferB]], inf), 2, inf),
            \buffer, [~bufferA],
            \amp, 1,
            \fb, 0,
            \stereoOffset, 1,
            \legato, Prand([0.5, 1], inf),
            \globalLag, 16,
            \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf),
            // \freq, 30,
            // \freq, 10,
            \sweep, 16,
            \size, Prand([1024, 4096, 2048], inf ),
            // \size, \512,
            // \cycleOffset, 2,
            // \cycleRate, Pseq([0.2, 0.5, 2] * 1, inf),

            \dur, Prand([2, Rest(2)], inf),
            // \dur, 2,
            \atk, 1,
            \dec, 0.8,
            \sus, 1,
            \rel, 2,
            \lpf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),

            // \dur, 0.25,
            // \atk, 0.1,
            // \dec, 0.2,
            // \sus, 0,
            // \rel, 0,

            \gain, 10,
            \out, ~bus2
        )
    ).play(t);
)

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

        fb = 0;
        size = 1024;
        freq =  60 * SampleDur.ir;

        // cyclePos = SinOsc.ar(0.1, 2pi).linlin(-1, 1, 0, 1);
        cyclePos = LFNoise2.ar(0.6).linlin(-1, 1, 0, 1);
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
        fbIdx = Select.ar(fb > 0, [DC.ar(-1), fbIdx]);
	    BufWr.ar(currentSample, ~buffer, fbIdx);

        sig = LeakDC.ar(sig) * 0.1;

        sig ! 2;
    }.play;
)


(
// scale modulation depth of modulators between 0 and 1
var modScaleBipolarUp = { |modulator, value, amount|
    value + (modulator * (1 - value) * amount);
};

SynthDef(\dualOscOS, {
    
    var param, calcWavetableData;
    var wavetableDataA, wavetableDataB;
    var sigs, sig;
    
    // Global parameter function
    param = { |chainID, name, default, spec|
        var paramName = "%_%".format(name, chainID).asSymbol;
        NamedControl.kr(paramName, default, lags: 0.02, fixedLag: true, spec: spec);
    };
    
    // Function to calculate wavetable data
    calcWavetableData = { |chainID, phaseOffset|
        
        var tableIndexMF, tableIndexMod, tableIndex;
        var wavetable, sizeOfTable, pmIndex, pmFltRatio;
        var freq, phase;
        
        freq = param.(chainID, \freq, 440, spec: ControlSpec(1, 1000, \exp));
        phase = Phasor.ar(DC.ar(0), freq * SampleDur.ir);
        
        /////////////////////////////////////////////////////////////////////////////////
        
        // Create table index modulation
        tableIndexMF = param.(chainID, \tableIndexMF, 0.1, ControlSpec(0.1, 1));
        tableIndexMod = { |phase|
            SinOsc.ar(tableIndexMF, phase + phaseOffset * pi)
        };
        
        tableIndex = modScaleBipolarUp.(
            modulator: tableIndexMod.(0.5),
            value: param.(chainID, \tableIndex, 0, ControlSpec(0, 1)),
            amount: param.(chainID, \tableIndexMD, 1, ControlSpec(0, 1))
        );
        
        // table params
        wavetable = param.(chainID, \sndBuf, 0);
        sizeOfTable = BufFrames.kr(wavetable) / 2048;
        
        // Phase modulation params
        pmIndex = param.(chainID, \pmIndex, 0, ControlSpec(0, 5));
        pmFltRatio = param.(chainID, \pmFltRatio, 1, ControlSpec(1, 5));
        
        /////////////////////////////////////////////////////////////////////////////////
        
        (
            phase: phase,
            pmIndex: pmIndex,
            pmFltRatio: pmFltRatio,
            wavetable: wavetable,
            sizeOfTable: sizeOfTable,
            tableIndex: tableIndex
        );
        
    };
    
    wavetableDataA = calcWavetableData.(\A, 0);
    wavetableDataB = calcWavetableData.(\B, 1);
    
    sigs = DualOscOS.ar(

        bufnumA: wavetableDataA[\wavetable],
        phaseA: wavetableDataA[\phase],
        numCyclesA: wavetableDataA[\sizeOfTable],
        cyclePosA: wavetableDataA[\tableIndex],

        bufnumB: wavetableDataB[\wavetable],
        phaseB: wavetableDataB[\phase],
        numCyclesB: wavetableDataB[\sizeOfTable],
        cyclePosB: wavetableDataB[\tableIndex],

        pmIndexA: wavetableDataA[\pmIndex],
        pmIndexB: wavetableDataB[\pmIndex],
        pmFilterRatioA: wavetableDataA[\pmFltRatio],
        pmFilterRatioB: wavetableDataB[\pmFltRatio],

        oversample: 0
    );

    sig = XFade2.ar(sigs[0], sigs[1], \chainMix.kr(0.5, spec: ControlSpec(0, 1)) * 2 - 1);
    
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = sig * \amp.kr(-25, spec: ControlSpec(-35, -5)).dbamp;
    sig = sig * Env.asr(0.001, 1, 0.001).ar(Done.freeSelf, \gate.kr(1));

    sig = LeakDC.ar(sig);
    sig = Limiter.ar(sig);
    Out.ar(\out.kr(0), sig);
}).add;
)

// create a wavetable (or use your own)
(
t = Signal.sineFill(2048, [1], [0]);
u = Signal.sineFill(2048, 1.0/((1..512)**2)*([1,0,-1,0]!128).flatten);
w = Signal.sineFill(2048, 1.0/(1..512)*([1,0]!256).flatten);
x = Signal.sineFill(2048, 1.0/(1..512));
v = t.addAll(u).addAll(w).addAll(x);

~buffer = Buffer.loadCollection(s, v);
)

(
x = Synth(\dualOscOS, [
    
    \sndBuf_A, ~buffer,    
    \freq_A, 440, 
    
    \tableIndexMF_A, 2,
    \tableIndexMD_A, 1,
    \tableIndex_A, 0,
    
    \pmFltRatio_A, 0.1, 
    \pmIndex_A, 4,
    
    ///////////////////////
    
    \sndBuf_B, ~buffer,
    \freq_B, 550, 

    \tableIndexMF_B, 2, 
    \tableIndexMD_B, 1, 
    \tableIndex_B, 0,
    
    \pmFltRatio_B, 2, 
    \pmIndex_B, 1, 
    
    ///////////////////////
    
    \chainMix, 1,
    \pan, 0,
    \amp, -25,
    \out, 0,
    
]);
)

x.free;