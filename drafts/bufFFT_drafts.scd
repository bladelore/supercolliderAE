(
~makeSpec = {|file, dict, fftSize, overlaps=2|
	// var fftQuant = 2**round(log2(fftSize));
	~clearAnalysisDict.(dict);
	dict.put(\filepath, file);
	dict.put(\file, Buffer.read(s, file));
	dict.put(\fftSize, fftSize);
	dict.put(\overlaps, overlaps);
	dict.put(\analysis, Array.fill(overlaps, {Buffer.alloc(s, fftSize)}));
};
)

(
a=Dictionary();
~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill Origins/Meltdown/Ac_70_kit.wav", a, 16384, 2)
)
Synth(\fftStretch_mono, [buf: a.at(\file), analysis: a.at(\analysis), fftSize: a.at(\fftsize), rate: 0.5, offset: 0])

Synth(\fftStretch_magFilter_mono, [buf: a.at(\file), analysis: a.at(\analysis), fftSize: a.at(\fftsize), rate: 0.4, filter: 2])

(
SynthDef(\fftStretch_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: 0,
		end: BufFrames.kr(buf),
		//trig: gate,
		// resetPos: startsamp
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
    chain = BufFFT(chain);
	chain = PV_Diffuser(chain, chain>(-1));
	// chain = PV_MagAbove(chain, MouseX.kr(0, 10));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fftStretch_magFilter_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: 0,
		end: BufFrames.kr(buf),
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
    chain = BufFFT(chain);

	// chain = PV_Mag(chain, \filter.kr(20));
	chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
	// chain = PV_Compander(chain, 1, 0.1, 1.0);
	chain = PV_Diffuser(chain, chain>(-1));


    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

)

(
SynthDef(\fftStretch_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;
	var bufFrames, trigRate, t, dur, offset, startsamp, endsamp, sampsDur, posPhasor;
	var polarity, polarityProb, pan, grains;

	bufFrames = BufFrames.ir(buf);
	offset = \offset.kr(0);
	startsamp = offset / bufFrames;
	sampsDur = bufFrames - startsamp;

	posPhasor = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: bufFrames,
		//trig: gate,
		resetPos: startsamp
	);

	posPhasor = Wrap.ar(posPhasor + (\pos.kr(0) * sampsDur), startsamp, bufFrames);

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);
    chain = BufFFT_BufCopy(chain, buf, posPhasor, BufRateScale.kr(buf));
    chain = BufFFT(chain);
	chain = PV_Diffuser(chain, chain>(-1));
	// chain = PV_MagAbove(chain, MouseX.kr(0, 10));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;
)
