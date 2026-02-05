(
SynthDef(\modsaw_mono, {|freq, gate=1|
	var nvoices1 = 5;
	var detune1 = 50;

	var osc1 = {
		var m = 2**(detune1/1200).rand2;
		var lfo1 = SinOsc.ar(1.rand).range(-1,1);
		var lfo2 = LFTri.ar(0.75.rand).range(0,1);
		NeoVarSawOsc.ar(m * freq, lfo1, lfo2);
	}.dup(nvoices1);

	osc1 = osc1 * \gain.kr(0).dbamp * \amp.kr(1);

	Out.ar(0, Splay.ar(osc1)*0.5);
}).add;
)

Synth(\modsaw_mono, [freq: 440])
1.rand

Ndef.clear;

(
Ndef(\lfo3, { LFSaw.kr(\lfofreq.kr(0.01)).bipolar * 0.2});
Ndef(\padFreqs, {([404, 404/2, 404/5, 404/7])+LFNoise2.kr(0.1!4, 15)});
Ndef(\padAmps, {[0.4, 0.3, 0.2, 0.5]+LFNoise2.kr(0.1!4, 0.1)});
Ndef(\padCut, {(10000 * Ndef.kr(\lfo3)).clip(20,20000)});
Ndef(\padQ, 0.8);

Ndef(\pad, {
	LeakDC.ar(
	Splay.ar(
		BLowPass4.ar(
			Blip.ar(
				Ndef.ar(\padFreqs),
				Ndef.ar(\padAmps)
			),
			Ndef.ar(\padCut),//cutoff freq for filter
			Ndef.ar(\padQ)//q for filter
		)
	)) * 0.25
}).play;
)



Pmono(\driftingSines_mono, \amp, 1, \freq, 404).play(t);

Synth(\driftingSines_mono, [freq: 404]);

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