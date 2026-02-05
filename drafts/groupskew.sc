

(
	~arrayDelta = {|array|
		var cumSum = List.new;
		var sum = 0.0;
		var arrSize;
	
		cumSum.add(0);
	
		array.do { |item, i|
			sum = sum + item;
			cumSum.add(sum);
		};
	
		cumSum = cumSum.asArray; 
		cumSum.removeAt(cumSum.size-1);
		cumSum
	};

	~lcmIndices = {|idx, num, denom|
		var lcm = num*denom;
		// var lcm = lcm(num,denom);
		var lcmSeq = (0..(lcm-1));
		var indices = (0..(denom-1)) * num;
		lcmSeq.at((indices + idx).wrap(0, lcmSeq.size));
	};

	~eventSkew = {|patt, group=1, skew=0, min=0.1, curve=\lin, numEvents, eventArr|
		var durList, durSize, durSum;
		var env, skewArr, newDur, newDelta;
		
		durList = List.new;
	
		//get durations as array for group
		numEvents.do({|i|
			var thisKey = eventArr[i].at('groupcountLCM');
			var thisDur = eventArr[i].at('dur');
			if(thisKey == group){
				durList.add(thisDur);
			};
		});
	
		durList = durList.asArray;
		durSize = durList.size;
		
		//skew range -1..1
		skewArr = if(skew >= 0){[skew, min]}{[min, abs(skew)]};
	
		env = Env(skewArr, [durSize], curve);
		//(durations[n] * sampled envelope[n]) normalised between 1 / scaled to duration of group
		newDur = (durList * env.at((0..durSize-1))).normalizeSum * durList.sum;
		newDelta = ~arrayDelta.(newDur);
		//replace events at group with skewed rhythm
		Pfunc({|ev|
			if(ev[\groupcountLCM] == group) 
			{ 	
				var thisDur = newDur[ev[\eventcount] - 1];
				ev[\dur] = thisDur;
				ev[\groupdelta] = newDelta[ev[\eventcount] - 1];				
			} 
			{ev = ev}
		});
	};

	~cycleSkew = {|patt, skew=0, min=0.1, curve=\lin, numEvents, eventArr|
		var durList, durSize, durSum;
		var env, skewArr, newDur;
		
		durList = List.new;
	
		//get durations as array for group
		numEvents.do({|i|
			var thisDur = eventArr[i].at('dur');
			durList.add(thisDur);
		});
	
		durList = durList.asArray;
		durSize = durList.size;
		
		//skew range -1..1
		skewArr = if(skew >= 0){[skew, min]}{[min, abs(skew)]};
	
		env = Env(skewArr, [durSize], curve);
		//(durations[n] * sampled envelope[n]) normalised between 1 / scaled to duration of group
		newDur = (durList * env.at((0..durSize-1))).normalizeSum * durList.sum;
		
		//replace events at group with skewed rhythm
		Pfunc({|ev|
			ev[\dur] = newDur[ev[\cyclecount] - 1];
		});
	};

	~pSkew = {|patt, key, group, skew=0, min=0.1, curve=\lin|
		var chain, idxList, chainList;
		var eventParams, numEvents, pattSize, subdivSize, eventArr, groupLCM;
		var arrSkew, arrCurve;
		var thisKey = key.key;
		
		//get all params
		eventParams = Pn(patt.value, inf).asStream.next(()).asDict;

		case{thisKey=='eventcount'}
		{
			pattSize = eventParams.at('groupcycle');
			subdivSize = eventParams.at('subdivSize');
			numEvents = eventParams.at('numeventsLCM');
			eventArr = patt.value.asStream.nextN(numEvents,());
			group = group.asArray.wrap(1, pattSize).asSet.asArray;
			//wrap group idx to pattern size and remove duplicates
			if(pattSize != subdivSize){

				//get idx multiples of group idx
				idxList = List.new;
				group.do{|elem|
					idxList.add(~lcmIndices.(elem - 1, pattSize, subdivSize) + 1);
				};
				group = idxList.asArray.flat.sort;
			};
			
			if(group.size > 1){
				chainList = List.new;
				// skew for each group idx in passed array
				group.size.do({|i|
					//if array, wrap array values for other params
					if(skew.isKindOf(Array)){ arrSkew = skew[i.wrap(0, skew.size - 1)]}{arrSkew = skew};
					if(curve.isKindOf(Array)){ arrCurve = curve[i.wrap(0, curve.size - 1)]}{arrCurve = curve};
					//add to list
					chainList.add(~eventSkew.(patt, group[i], arrSkew, min, arrCurve, numEvents, eventArr));
				});
				//expand list, applying function to each group
				chain = Pchain(*chainList);
			} {
				chain = ~eventSkew.(patt, group[0], skew, min, curve, numEvents, eventArr);
			}
		}

		{thisKey=='cyclecount'}
		{
			//get array of events from pattern
			numEvents = eventParams.asDict.at('numevents');
			eventArr = patt.value.asStream.nextN(numEvents,());

			if(skew.isKindOf(Array)){ arrSkew = skew[0]}{arrSkew = skew};
			if(curve.isKindOf(Array)){ arrCurve = curve[0]}{arrCurve = curve};

			chain = ~cycleSkew.(patt, arrSkew, min, arrCurve, numEvents, eventArr);
		}
	};
)

(
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

//even lengths - works
//uneven lengths - works
//even lengths subgrouped - doesnt work
Pdef(\p1,
	~makeSubdivision.(
		PlaceAll([[1, 1.5], [1.5, Rest(1.5)], 1, [Rest(0.5), 1]], inf),
		PlaceAll([[4, 1], 4, 0], inf)
	)
);

Pdef(\skewed,
	Pbind(
		\freq, 55.0,
		\sweep, 4,
		\roomsize, 1+(Pkey(\groupdelta)*4),
		\reverbtime, 1+(Pkey(\groupdelta)*4),
		\atk, Pkey(\groupdelta).linlin(0,1, 0.05, 0.3),
		\dec, 0.20000000298023,
		\gain, -6.0,
		\pan, 0.0,
		\amp, 1.0,
	) <>
	// ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 2, 4], mod: 3) <>
	~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: 1, skew: [1,2], curve: \exp) <> 
	Pdef(\p1)
	<> Pbind(
		\instrument, \kick2,
	)
).play(t);
)
(
Pdef(\p1,
	~makeSubdivision.(
		PlaceAll([1, 1, [1,2,3], 1], inf),
		// PlaceAll([1.5, 1.5, 2], inf),
		PlaceAll([[4, 3], 3, 3], inf)
	)
).trace.play(t);)

lcm(6,8)


(	
	var group = [1,2];
	var pattSize = 6;
	var subdivSize = 8;
	
	//get idx multiples of group idx
	var idxList = List.new;
	group = group.wrap(1, pattSize).asSet.asArray;
	group.do{|elem|
		idxList.add(~lcmIndices.(elem, pattSize, subdivSize));
	};
	group = idxList.asArray.flat.sort + 1;
	group;
)


(
~lcmIndices = {|idx, num, denom|
	var lcm = num*denom;
	// var lcm = lcm(num,denom);
	var lcmSeq = (0..(lcm-1));
	var indices = (0..(num-1)) * denom;
	lcm.postln;
	lcmSeq.postln;
	indices.postln;
	lcmSeq.at((indices + idx).wrap(0, lcmSeq.size));
};
a = ~lcmIndices.(0, 4, 8);
)

1.asArray.wrap(1, 4).asSet.asArray;
