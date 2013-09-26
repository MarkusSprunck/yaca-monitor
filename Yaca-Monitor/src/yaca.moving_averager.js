/**
 *
 * Calculates the moving average of a number of data
 * points.  
 *
 */

function MovingAverager(length) {
	this.maxLength = length;
	this.nums = [];
}

MovingAverager.prototype.setValue = function(num) {
	this.nums.push(num);
	if (this.nums.length > this.maxLength) {
		this.nums.splice(0, this.nums.length - this.maxLength);  		
	}	
};

MovingAverager.prototype.getValue = function() {
	var sum = 0.0;
	for (var i in this.nums) {
		sum += this.nums[i];
	}				
	return (sum / this.nums.length);
};