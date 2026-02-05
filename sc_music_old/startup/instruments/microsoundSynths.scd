(
var rampToSlope = { |phase|
	var history = Delay1.ar(phase);
	var delta = (phase - history);
	delta.wrap(-0.5, 0.5);
};

var rampToTrig = { |phase|
	var history = Delay1.ar(phase);
	var delta = (phase - history);
	var sum = (phase + history);
	var trig = (delta / sum).abs > 0.5;
	Trig1.ar(trig, SampleDur.ir);
};

var getSubSampleOffset = { |phase, trig|
	var slope = rampToSlope.(phase);
	var sampleCount = phase - (slope < 0) / slope;
	Latch.ar(sampleCount, trig);
};

var accumulatorSubSample = { |trig, subSampleOffset|
	var accum = Duty.ar(SampleDur.ir, trig, Dseries(0, 1));
	accum + subSampleOffset;
};

SynthDef(\pulsar_mono, {
	var flux, triggerRate, stepPhase, stepSlope, stepTrigger, subSampleOffsets, accumulator, polarity;
	var windowSlope, windowPhase, maxOverlap, overlap;
	var modSlope, modPhase, pmod;
	var grainFreq, grainSlope, grainPhase, grainWindow, sig;

	flux = LFDNoise3.ar(\fluxMF.kr(10)) ! 2;
	flux = 2 ** (flux * \fluxMD.kr(3));

	triggerRate = \triggerRate.kr(100) * flux;
	stepPhase = (Phasor.ar(0, triggerRate * SampleDur.ir) - SampleDur.ir).wrap(0, 1);
	stepTrigger = rampToTrig.(stepPhase);
	stepSlope = rampToSlope.(stepPhase);

	subSampleOffsets = getSubSampleOffset.(stepPhase, stepTrigger);
	accumulator = accumulatorSubSample.(stepTrigger, subSampleOffsets);

	grainFreq = \grainFreq.kr(50) * flux;
	grainSlope = grainFreq * SampleDur.ir;
	grainPhase = (grainSlope * accumulator).wrap(0, 1);

	maxOverlap = grainSlope / Latch.ar(stepSlope, stepTrigger);
	overlap = min(\overlap.kr(100), maxOverlap);
	windowSlope = grainSlope / max(0.001, overlap);
	windowPhase = (windowSlope * accumulator).clip(0, 1);

	modSlope = grainSlope * \pmRatio.kr(150);
	modPhase = (modSlope * accumulator).wrap(0, 1);

	grainWindow = IEnvGen.ar(Env([0, 1, 0], [0.03, 0.97], [4.0, -4.0]), windowPhase);

	pmod = sin(modPhase * 2pi) * \pmIndex.kr(1);
	pmod = OnePole.ar(pmod, exp(-2pi * modSlope));

	sig = sin(grainPhase + (pmod / 2pi) * 2pi);

	polarity = ~velvet.(stepTrigger, \density.kr(1), 1 - \polarityMod.kr(0));

	sig = sig * grainWindow * polarity;

	sig!2 * 0.1;
	sig = sig.sanitize;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\convolved_pulsar_mono, {|buf|
	var flux, triggerRate, stepPhase, stepSlope, stepTrigger, subSampleOffsets, accumulator, polarity;
	var windowSlope, windowPhase, maxOverlap, overlap;
	var modSlope, modPhase, pmod;
	var grainFreq, grainSlope, grainPhase, grainWindow, sig;

	flux = LFDNoise3.ar(\fluxMF.kr(10)) ! 2;
	flux = 2 ** (flux * \fluxMD.kr(3));

	triggerRate = \triggerRate.kr(100) * flux;
	stepPhase = (Phasor.ar(0, triggerRate * SampleDur.ir) - SampleDur.ir).wrap(0, 1);
	stepTrigger = rampToTrig.(stepPhase);
	stepSlope = rampToSlope.(stepPhase);

	subSampleOffsets = getSubSampleOffset.(stepPhase, stepTrigger);
	accumulator = accumulatorSubSample.(stepTrigger, subSampleOffsets);

	grainFreq = \grainFreq.kr(50) * flux;
	grainSlope = grainFreq * SampleDur.ir;
	grainPhase = (grainSlope * accumulator).wrap(0, 1);

	maxOverlap = grainSlope / Latch.ar(stepSlope, stepTrigger);
	overlap = min(\overlap.kr(100), maxOverlap);
	windowSlope = grainSlope / max(0.001, overlap);
	windowPhase = (windowSlope * accumulator).clip(0, 1);

	modSlope = grainSlope * \pmRatio.kr(150);
	modPhase = (modSlope * accumulator).wrap(0, 1);

	grainWindow = IEnvGen.ar(Env([0, 1, 0], [0.03, 0.97], [4.0, -4.0]), windowPhase);

	pmod = sin(modPhase * 2pi) * \pmIndex.kr(1);
	pmod = OnePole.ar(pmod, exp(-2pi * modSlope));

	sig = sin(grainPhase + (pmod / 2pi) * 2pi);

	polarity = ~velvet.(stepTrigger, \density.kr(1), 1 - \polarityMod.kr(0));

	sig = sig * grainWindow * polarity;
	// sig = Convolution2L.ar(sig, buf, Trig.kr(1, dur: \sustainTime.kr(0.01)), 2**round(log2(\window.kr(512))), 1);
	sig = Convolution2L.ar(sig, buf, stepTrigger, 2**round(log2(\window.kr(512))), 1);

	sig!2 * 0.1;
	sig = sig.sanitize;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

)