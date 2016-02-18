package eu.w4.contrib.inflow.data.zipncities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.w4.common.exception.CheckedException;
import eu.w4.common.log.Logger;
import eu.w4.common.log.LoggerFactory;
import eu.w4.engine.client.Filter;
import eu.w4.inflow.client.dataconnector.DataConnectorCriterionFilter;
import eu.w4.inflow.client.dataconnector.DataConnectorDefinition;
import eu.w4.inflow.client.dataconnector.DataConnectorFilter;
import eu.w4.inflow.client.dataconnector.DataConnectorResult;
import eu.w4.inflow.core.dataconnector.InflowDataConnectorContext;
import eu.w4.inflow.core.dataconnector.InflowDataConnectorDriver;
import eu.w4.inflow.core.dataconnector.InflowDataConnectorInitContext;

public class CitiesDataConnector implements InflowDataConnectorDriver
{

  private Logger _logger = LoggerFactory.getLogger(CitiesDataConnector.class.getName());
  private InflowDataConnectorInitContext _context;
  private List<Entry> _entries;

  @Override
  public void init(InflowDataConnectorInitContext context)
    throws CheckedException
  {
    _context = context;

    final InputStream inputStream = this.getClass().getResourceAsStream("data.csv");
    try
    {
      if (inputStream != null)
      {
        try
        {
          _entries = readData(inputStream);
        }
        catch (final IOException e)
        {
          _logger.error("An error occured while reading data", e);  
        }
      }
      _logger.info("Successfully loaded " + _entries.size() + " city entries.");
    }
    finally
    {
      try
      {
        inputStream.close();
      }
      catch (final IOException finalException)
      {
        _logger.error("An error occured while trying to close a stream", finalException);
      }
    }
  }

  private List<Entry> readData(InputStream inputStream)
    throws IOException
  {
    final List<Entry> entries = new ArrayList<Entry>(40000);
    final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
    {
      if (line.startsWith("#") || line.trim().isEmpty())
      {
        continue;
      }
      final String[] columns = line.split(";");
      if (columns.length < 4)
      {
        continue;
      }
      final Entry entry = new Entry();
      entry.setInsee(columns[0]);
      entry.setName(columns[1]);
      entry.setZip(columns[2]);
      entries.add(entry);
    }
    return entries;
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public DataConnectorDefinition getDataConnectorDefinition(final InflowDataConnectorContext inflowDataConnectorContext)
    throws CheckedException
  {
    final DataConnectorDefinition dataConnectorDefinition = inflowDataConnectorContext.newDataConnectorDefinition();

    final Map<String, Class<?>> criterions = new HashMap<String, Class<?>>();
    criterions.put("zip", String.class);
    criterions.put("name", String.class);

    final Map<String, Class<?>> values = new HashMap<String, Class<?>>();
    values.put("insee", String.class);
    values.put("zip", String.class);
    values.put("name", String.class);

    dataConnectorDefinition.setCriterions(criterions);
    dataConnectorDefinition.setValueKeys(values);
    dataConnectorDefinition.setUniqueKeyName("insee");
    return dataConnectorDefinition;
  }

  @Override
  public List<DataConnectorResult> getData(final InflowDataConnectorContext inflowDataConnectorContext,
                                           final DataConnectorFilter dataConnectorFilter)
    throws CheckedException
  {
    final List<Entry> selectedEntries = new ArrayList<Entry>(_entries);
    final LinkedList<Filter> filters = new LinkedList<Filter>();
    if (dataConnectorFilter != null)
    {
      filters.add(dataConnectorFilter);
    }
    while (!filters.isEmpty())
    {
      final Filter filter = filters.poll();
      if (filter.getAndFilters() != null)
      {
        filters.addAll(filter.getAndFilters());
      }
      if (filter.getOrFilters() != null && !filter.getOrFilters().isEmpty())
      {
        throw new CheckedException("OR filters are not supported");
      }
      if (filter.getNotFilters() != null && !filter.getNotFilters().isEmpty())
      {
        throw new CheckedException("NOT filters are not supported");
      }
      if (filter instanceof DataConnectorFilter)
      {
        continue;
      }
      if (!(filter instanceof DataConnectorCriterionFilter))
      {
        throw new CheckedException("Filters must be instance of DataConnectorCriterionFilter");
      }
      final DataConnectorCriterionFilter criterionFilter = (DataConnectorCriterionFilter) filter;
      if ("zip".equals(criterionFilter.getName()))
      {
        if (_logger.isDebugEnabled())
        {
          _logger.debug("[SEARCH] zip = "  + criterionFilter.getValue());
        }
        final String value = (String) criterionFilter.getValue();
        if (value.length() < 3)
        {
          continue;
        }
        for (final Iterator<Entry> i = selectedEntries.iterator();i.hasNext();)
        {
          final Entry e = i.next();
          if (!e.getZip().startsWith(value))
          {
            i.remove();
          }
        }
      }
      else if ("name".equals(criterionFilter.getValue()))
      {
        if (_logger.isDebugEnabled())
        {
          _logger.debug("[SEARCH] cityname = "  + criterionFilter.getValue());
        }
        final String value = (String) criterionFilter.getValue();
        if (value.length() < 3)
        {
          continue;
        }
        for (final Iterator<Entry> i = selectedEntries.iterator();i.hasNext();)
        {
          final Entry e = i.next();
          if (!e.getName().startsWith(value))
          {
            i.remove();
          }
        }
      }
    }
    if (selectedEntries.size() == _entries.size())
    {
      if (_logger.isDebugEnabled())
      {
        _logger.debug("[SEARCH] returned empty result because not enough filtered");
      }
      return Collections.emptyList();
    }
    final List<DataConnectorResult> results = new ArrayList<DataConnectorResult>(selectedEntries.size());
    for (final Entry entry : selectedEntries)
    {
      final DataConnectorResult result = inflowDataConnectorContext.newDataConnectorResult();
      result.setValues(entry.toMap());
      results.add(result);
    }
    if (_logger.isDebugEnabled())
    {
      _logger.debug("[SEARCH] returned " + results.size() + " values");
    }
    return results;
  }
}
