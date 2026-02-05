s.quit

(
~makeSpec = {|file, dict, fftSize, overlaps=2|
    dict.put(\filepath, file);
    dict.put(\file, Buffer.readChannel(s, file, channels: [0]));
    dict.put(\fftSize, fftSize);
	//overlaps hardcoded
	// dict.put(\overlaps, overlaps);
    dict.put(\analysis, Array.fill(overlaps, {Buffer.alloc(s, fftSize)}).asRef);
};

SynthDef(\fftStretch_mono, {|buf, fftSize, analysis=#[0, 1]|
    var sig;
    var chain;
    var pos;
    var startsamp, endsamp;
    var bufFrames = BufFrames.kr(buf);

	//args equiv to 1/overlap, 0..overlap, overlap for overlap=2
	chain = BufFFTTrigger(analysis, 0.5, [0, 1], 2);

	//or this?
	// chain = BufFFTTrigger(\analysis.ir(#[0, 1]), 0.5, [0, 1], 2);

    startsamp = (\pos.kr(0) * bufFrames);
    endsamp = startsamp + (\len.kr(1) * bufFrames);

    pos = Phasor.ar(
        rate: \rate.kr(1),
        start: startsamp,
        end: endsamp,
    );

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
    chain = BufFFT(chain);
    chain = PV_Diffuser(chain, chain>(-1));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
	sig = sig.sanitize;
    Out.ar(\out.kr(0), sig);
}).add;
)

~specBuff.at(\analysis)

(
~specBuff=Dictionary();
// ~makeSpec.(Platform.resourceDir +/+ "sounds/a11wlk01.wav", ~specBuff, 8192, 2)
~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 8192, 2)
)

//WORKS
(
Synth(\fftStretch_mono,
	[buf: ~specBuff.at(\file), analysis: ~specBuff.at(\analysis), fftSize: ~specBuff.at(\fftsize), rate: 0.01, pos: 0.3, len: 0.05])
)

//CRASH
(
Pmono(\fftStretch_mono,
	\amp, 1,
    \buf, ~specBuff.at(\file),
    \analysis, [~specBuff.at(\analysis)],
    \fftSize, ~specBuff.at(\fftSize),
    \rate, ~pmodenv.(Pseq([0.01, 0.5],inf), Pseq([1, 1, 2], inf)),
    \pos, 0.3,
    \len, 0.05,
).trace.play(t);
)