var
  timers = [],
  window = {
    document: {
      getElementById: function(id) {
        return [];
      },
      getElementsByTagName: function(tagName) {
        return [];
      }
    },
    location: {
      protocol: 'file:',
      hostname: 'localhost',
      port: '80'
    },
    setInterval: function(fn, time) {
      var num = timers.length;
      timers[num] = fn.call(this, null);
      return num;
    }
  },
  document = window.document,
  location = window.location,
  setInterval = window.setInterval;