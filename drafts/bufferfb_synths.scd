b = Buffer.alloc(s, s.sampleRate, 5);

(
{
var writeSig, writePos, readPos;
writeSig = SinOsc.ar({ LFDNoise3.kr(1).exprange(10, 100) } ! 5);
writePos = Phasor.ar(0.0, MouseX.kr(0,200), 0.0, BufFrames.kr(b));
BufWr.ar(writeSig, b, writePos);
readPos = Phasor.ar(0.0, MouseY.kr(0,100), 0.0, BufFrames.kr(b));
Splay.ar(BufRd.ar(5, b, readPos));
}.play;
)

// scope
(
{
	var outLevel = 0.2;
	var freq = 100;
	var proportion = LFTri.kr(0.25).range(0.5, 1);
	Env([0, 0, 1, 0, 0], [0, proportion, 1 - proportion, 0], releaseNode: 3, loopNode: 0)
	.ar(gate: 1, timeScale: freq.reciprocal, levelScale: outLevel);
}.scope;
)
// plot
(
{
	var freq = 10;
	var proportions =  [0.5, 0.7, 0.9, 1];
	proportions.collect({arg prop, i;
		Env([0, 0, 1, 0, 0], [0, prop, 1 - prop, 0], releaseNode: 3, loopNode: 0)
		.kr(gate: 1, timeScale: freq.reciprocal);
	})
}.plot(0.5);
)
