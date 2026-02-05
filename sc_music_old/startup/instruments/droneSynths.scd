(
SynthDef(\driftingSines_mono, {
	var sig;
	var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
	var freq = \freq.kr(404);
	var freqs = ([freq, freq/2, freq/5, freq/7] + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
	var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
	var q = 0.8;

	sig = Blip.ar(freqs, \numHarm.kr(0)) * amps;
	sig = BLowPass4.ar(sig, (5000 * lfo), q);
	sig = Splay.ar(sig);
	sig = LeakDC.ar(sig).sanitize;

	sig = sig * 0.25;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;
)